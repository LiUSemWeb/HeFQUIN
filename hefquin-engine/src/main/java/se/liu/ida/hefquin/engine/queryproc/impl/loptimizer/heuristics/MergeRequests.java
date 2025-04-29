package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.utils.SPARQLRequestUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * Merges subplans that consists of multiple requests to the same federation
 * member if such a merge is possible.
 *
 * In particular, a join over two requests is merged into a single request
 * operator if i) the two requests are triple pattern requests (which can be
 * merged into a BGP request and the federation member supports BGP requests,
 * or ii) one of the two requests is a BGP request and the other one also a
 * BGP request or a triple pattern request.
 *
 * Another possibility for merging is a join over two arbitrary SPARQL pattern
 * requests for a SPARQL endpoint. For SPARQL endpoints, this heuristic even
 * tries to push other operators into requests (filters, unions, optional).
 *
 * The aforementioned join-related merge is performed not only for binary join
 * operators but also for tpAdd/bgpAdd operators over request operators, as well
 * as for pairs of requests under a multi-join. In the latter case, the multi-join
 * operator is replaced by the merged request operator only if there are no other
 * subplans under that multi-join operator.
 *
 * The merging is applied recursively in a bottom-up fashion, which means that,
 * after performing one merge step, another one may become available and will be
 * done.
 */
public class MergeRequests implements HeuristicForLogicalOptimization
{
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		// First, apply the heuristic recursively to all subplans. When doing
		// so, keep track of whether there was at least one subplan that was
		// actually rewritten.
		boolean subPlansDiffer = false; // will be set to true if at least one of the subplans is rewritten
		final List<LogicalPlan> rewrittenSubPlans = new ArrayList<>(numberOfSubPlans);
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan subPlan = inputPlan.getSubPlan(i);
			final LogicalPlan rewrittenSubPlan = apply(subPlan);
			rewrittenSubPlans.add(rewrittenSubPlan);

			if ( ! subPlansDiffer ) { // check for equivalence only if necessary
				if ( ! subPlan.equals(rewrittenSubPlan) )
					subPlansDiffer = true;
			}
		}

		// Next, apply the heuristic to the root of the plan if possible.
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( rootOp instanceof LogicalOpRequest )
		{
			// nothing to do here - we are in a leaf node
		}
		else if ( rootOp instanceof LogicalOpTPAdd tpAdd )
		{
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if ( childOp instanceof LogicalOpRequest reqOp ) {
				final FederationMember fm = tpAdd.getFederationMember();
				if ( fm.getInterface().supportsBGPRequests() ) {
					if ( reqOp.getFederationMember().equals(fm) ) {
						return mergePatternIntoRequest( tpAdd.getTP(),
						                                fm,
						                                (SPARQLRequest) reqOp.getRequest() );
					}
				}
			}
		}
		else if ( rootOp instanceof LogicalOpBGPAdd bgpAdd )
		{
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if ( childOp instanceof LogicalOpRequest reqOp ) {
				final FederationMember fm = bgpAdd.getFederationMember();
				if ( reqOp.getFederationMember().equals(fm) ) {
					return mergePatternIntoRequest( bgpAdd.getBGP(),
					                                fm,
					                                (SPARQLRequest) reqOp.getRequest() );
				}
			}
		}
		else if ( rootOp instanceof LogicalOpGPAdd gpAdd )
		{
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if ( childOp instanceof LogicalOpRequest reqOp ) {
				final FederationMember fm = gpAdd.getFederationMember();
				if ( reqOp.getFederationMember().equals(fm) ) {
					return mergePatternIntoRequest( gpAdd.getPattern(),
					                                fm,
					                                (SPARQLRequest) reqOp.getRequest() );
				}
			}
		}
		else if ( rootOp instanceof LogicalOpTPOptAdd tpOptAdd )
		{
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if ( childOp instanceof LogicalOpRequest reqOp ) {
				final FederationMember fm = tpOptAdd.getFederationMember();
				if ( fm.getInterface().supportsSPARQLPatternRequests() ) {
					if ( reqOp.getFederationMember().equals(fm) ) {
						return mergeOptPatternsIntoRequest( Collections.singletonList(tpOptAdd.getTP()),
						                                    (SPARQLEndpoint) fm,
						                                    (SPARQLRequest) reqOp.getRequest() );
					}
				}
			}
		}
		else if ( rootOp instanceof LogicalOpBGPOptAdd bgpOptAdd )
		{
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if ( childOp instanceof LogicalOpRequest reqOp ) {
				final FederationMember fm = bgpOptAdd.getFederationMember();
				if ( fm.getInterface().supportsSPARQLPatternRequests() ) {
					if ( reqOp.getFederationMember().equals(fm) ) {
						return mergeOptPatternsIntoRequest( Collections.singletonList(bgpOptAdd.getBGP()),
						                                    (SPARQLEndpoint) fm,
						                                    (SPARQLRequest) reqOp.getRequest() );
					}
				}
			}
		}
		else if ( rootOp instanceof LogicalOpGPOptAdd gpOptAdd )
		{
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if ( childOp instanceof LogicalOpRequest reqOp ) {
				final FederationMember fm = gpOptAdd.getFederationMember();
				if ( fm.getInterface().supportsSPARQLPatternRequests() ) {
					if ( reqOp.getFederationMember().equals(fm) ) {
						return mergeOptPatternsIntoRequest( Collections.singletonList(gpOptAdd.getPattern()),
						                                    (SPARQLEndpoint) fm,
						                                    (SPARQLRequest) reqOp.getRequest() );
					}
				}
			}
		}
		else if ( rootOp instanceof LogicalOpFilter filterOp )
		{
			// A filter can be merged into a request operator if that request
			// is for a SPARQL endpoint.
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if ( childOp instanceof LogicalOpRequest reqOp ) {
				if ( reqOp.getFederationMember().getInterface().supportsSPARQLPatternRequests() ) {
					return mergeFilterIntoSPARQLEndpointRequest( filterOp,
					                                             (SPARQLEndpoint) reqOp.getFederationMember(),
					                                             (SPARQLRequest) reqOp.getRequest() );
				}
			}
		}
		else if ( rootOp instanceof LogicalOpBind bindOp )
		{
			// The BIND clause can be merged into a request operator if that request is for a SPARQL endpoint.
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if ( childOp instanceof LogicalOpRequest reqOp ) {
				if ( reqOp.getFederationMember().getInterface().supportsSPARQLPatternRequests() ) {
					return mergeBindIntoSPARQLEndpointRequest( bindOp,
					                                           (SPARQLEndpoint) reqOp.getFederationMember(),
					                                           (SPARQLRequest) reqOp.getRequest() );
				}
			}
		}
		else if (    rootOp instanceof LogicalOpLocalToGlobal
		          || rootOp instanceof LogicalOpGlobalToLocal )
		{
			// nothing to do here - if we have a vocabulary translation as root
			// operator, we do not attempt to merge it with its input operator
			// (unless that is another vocabulary translation, but that case is
			// covered by another rewriting rule)
		}
		else if ( rootOp instanceof LogicalOpUnion )
		{
			final LogicalOperator childOp1 = rewrittenSubPlans.get(0).getRootOperator();
			final LogicalOperator childOp2 = rewrittenSubPlans.get(1).getRootOperator();
			if (    childOp1 instanceof LogicalOpRequest reqOp1
			     && childOp2 instanceof LogicalOpRequest reqOp2 ) {
				final FederationMember fm = reqOp1.getFederationMember();
				if (    fm.getInterface().supportsSPARQLPatternRequests()
				     && reqOp2.getFederationMember().equals(fm) ) {
					return mergeSPARQLRequestsViaUnion( (SPARQLEndpoint) fm,
					                                    (SPARQLRequest) reqOp1.getRequest(),
					                                    (SPARQLRequest) reqOp2.getRequest() );
				}
			}
		}
		else if ( rootOp instanceof LogicalOpJoin )
		{
			final LogicalOperator childOp1 = rewrittenSubPlans.get(0).getRootOperator();
			final LogicalOperator childOp2 = rewrittenSubPlans.get(1).getRootOperator();
			if (    childOp1 instanceof LogicalOpRequest reqOp1
			     && childOp2 instanceof LogicalOpRequest reqOp2 ) {
				final FederationMember fm = reqOp1.getFederationMember();
				if (    fm.getInterface().supportsBGPRequests()
				     && reqOp2.getFederationMember().equals(fm) ) {
					return mergeSPARQLRequestsViaJoin( fm,
					                                   (SPARQLRequest) reqOp1.getRequest(),
					                                   (SPARQLRequest) reqOp2.getRequest() );
				}
			}
		}
		else if ( rootOp instanceof LogicalOpRightJoin )
		{
			final LogicalOperator childOp1 = rewrittenSubPlans.get(0).getRootOperator();
			final LogicalOperator childOp2 = rewrittenSubPlans.get(1).getRootOperator();
			if (    childOp1 instanceof LogicalOpRequest reqOp1
			     && childOp2 instanceof LogicalOpRequest reqOp2 ) {
				final FederationMember fm = reqOp1.getFederationMember();
				if (    fm.getInterface().supportsSPARQLPatternRequests()
				     && reqOp2.getFederationMember().equals(fm) ) {
					final SPARQLRequest optRequest = (SPARQLRequest) reqOp1.getRequest(); // the LHS is the
					final SPARQLGraphPattern optPattern = optRequest.getQueryPattern();   // optional part
					return mergeOptPatternsIntoRequest( Collections.singletonList(optPattern),
					                                    (SPARQLEndpoint) fm,
					                                    (SPARQLRequest) reqOp2.getRequest() );
				}
			}
		}
		else if ( rootOp instanceof LogicalOpMultiwayUnion )
		{
			assert numberOfSubPlans > 0;
			if ( numberOfSubPlans == 1 ) return rewrittenSubPlans.get(0);

			final List<LogicalPlan> newSubPlans = new ArrayList<>(numberOfSubPlans);
			final Map<FederationMember,List<LogicalPlan>> reqOnlyPlansPerFedMember = new HashMap<>();

			separateSubPlansOfMultiwayOps(rewrittenSubPlans, reqOnlyPlansPerFedMember, newSubPlans);

			boolean noChange = true;
			for ( final Map.Entry<FederationMember,List<LogicalPlan>> e : reqOnlyPlansPerFedMember.entrySet() ) {
				final FederationMember fm = e.getKey();
				final List<LogicalPlan> reqPlans = e.getValue();
				if ( fm.getInterface().supportsSPARQLPatternRequests() && reqPlans.size() > 1 ) {
					final LogicalPlan mergedSubPlan = mergeSPARQLRequestsViaUnion( (SPARQLEndpoint) fm, reqPlans );
					newSubPlans.add(mergedSubPlan);
					noChange = false;
				}
				else {
					newSubPlans.addAll(reqPlans);
				}
			}

			if ( noChange == false ) {
				if ( newSubPlans.size() == 1 )
					return newSubPlans.get(0);
				else
					return LogicalPlanUtils.createPlanWithSubPlans(rootOp, newSubPlans);
			}
		}
		else if ( rootOp instanceof LogicalOpMultiwayJoin )
		{
			assert numberOfSubPlans > 0;
			if ( numberOfSubPlans == 1 ) return rewrittenSubPlans.get(0);

			final List<LogicalPlan> newSubPlans = new ArrayList<>(numberOfSubPlans);
			final Map<FederationMember,List<LogicalPlan>> reqOnlyPlansPerFedMember = new HashMap<>();

			separateSubPlansOfMultiwayOps(rewrittenSubPlans, reqOnlyPlansPerFedMember, newSubPlans);

			boolean noChange = true;
			for ( final Map.Entry<FederationMember,List<LogicalPlan>> e : reqOnlyPlansPerFedMember.entrySet() ) {
				final FederationMember fm = e.getKey();
				final List<LogicalPlan> reqPlans = e.getValue();
				if ( reqPlans.size() > 1 ) {
					final LogicalPlan mergedSubPlan = mergeSPARQLRequestsViaJoin( (SPARQLEndpoint) fm, reqPlans );
					newSubPlans.add(mergedSubPlan);
					noChange = false;
				}
				else {
					newSubPlans.addAll(reqPlans);
				}
			}

			if ( noChange == false ) {
				if ( newSubPlans.size() == 1 )
					return newSubPlans.get(0);
				else
					return LogicalPlanUtils.createPlanWithSubPlans(rootOp, newSubPlans);
			}
		}
		else if ( rootOp instanceof LogicalOpMultiwayLeftJoin )
		{
			assert numberOfSubPlans > 0;
			if ( numberOfSubPlans == 1 ) return rewrittenSubPlans.get(0);

			final Iterator<LogicalPlan> it = rewrittenSubPlans.iterator();
			final LogicalPlan nonoptSubPlan = it.next();
			if ( nonoptSubPlan.getRootOperator() instanceof LogicalOpRequest nonoptReqOp ) {
				final FederationMember fm = nonoptReqOp.getFederationMember();
				if ( fm.getInterface().supportsSPARQLPatternRequests() ) {
					final List<LogicalPlan> newSubPlans = new ArrayList<>(numberOfSubPlans);
					final List<SPARQLGraphPattern> optPatterns = new ArrayList<>(numberOfSubPlans);
					while ( it.hasNext() ) {
						final LogicalPlan optSubPlan = it.next();
						if (    optSubPlan.getRootOperator() instanceof LogicalOpRequest optReqOp
						     && optReqOp.getFederationMember().equals(fm) ) {
							final SPARQLRequest optReq = (SPARQLRequest) optReqOp.getRequest();
							optPatterns.add( optReq.getQueryPattern() );
						}
						else {
							newSubPlans.add(optSubPlan);
						}

						if ( ! optPatterns.isEmpty() ) {
							final SPARQLRequest nonoptReq = (SPARQLRequest) nonoptReqOp.getRequest();
							return mergeOptPatternsIntoRequest(optPatterns, (SPARQLEndpoint) fm, nonoptReq);
						}
					}
				}
			}
		}
		else
		{
			throw new IllegalArgumentException( "unexpected type of logical operator: " + rootOp.getClass().getName() );
		}

		// Finally, if the heuristic was not applied to the root of
		// the plan, return the plan without changing its root, but
		// make sure to use the rewritten subplans if necessary.
		if ( subPlansDiffer ) {
			return LogicalPlanUtils.createPlanWithSubPlans(rootOp, rewrittenSubPlans);
		}
		else {
			return inputPlan;
		}
	}

	public static LogicalPlan mergeFilterIntoSPARQLEndpointRequest( final LogicalOpFilter filterOp,
	                                                                final SPARQLEndpoint ep,
	                                                                final SPARQLRequest req ) {
		final ExprList exprList = filterOp.getFilterExpressions();
		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(exprList);
		final SPARQLRequest mergedReq = new SPARQLRequestImpl(mergedPattern);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(ep, mergedReq);
		return new LogicalPlanWithNullaryRootImpl(reqOp);
	}

	public static LogicalPlan mergeBindIntoSPARQLEndpointRequest( final LogicalOpBind bindOp,
	                                                              final SPARQLEndpoint ep,
	                                                              final SPARQLRequest req ) {
		final VarExprList exprList = bindOp.getBindExpressions();
		final SPARQLGraphPattern mergedPattern = req.getQueryPattern().mergeWith(exprList);
		final SPARQLRequest mergedReq = new SPARQLRequestImpl(mergedPattern);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(ep, mergedReq);
		return new LogicalPlanWithNullaryRootImpl(reqOp);
	}

	protected LogicalPlan mergePatternIntoRequest( final SPARQLGraphPattern p,
	                                                          final FederationMember fm,
	                                                          final SPARQLRequest req ) {
		final SPARQLRequest mergedReq = SPARQLRequestUtils.merge(req, p);
		final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>(fm, mergedReq);
		return new LogicalPlanWithNullaryRootImpl(mergedReqOp);
	}

	protected LogicalPlan mergeSPARQLRequestsViaUnion( final SPARQLEndpoint fm,
	                                                   final SPARQLRequest req1,
	                                                   final SPARQLRequest req2 ) {
		final SPARQLGraphPattern p = new SPARQLUnionPatternImpl( req1.getQueryPattern(), req2.getQueryPattern() );
		return createPlanWithSingleRequestOp(p, fm);
	}

	protected LogicalPlan mergeSPARQLRequestsViaUnion( final SPARQLEndpoint fm,
	                                                   final List<LogicalPlan> reqPlans ) {
		final SPARQLUnionPatternImpl up = new SPARQLUnionPatternImpl();
		for ( final LogicalPlan reqPlan : reqPlans ) {
			final LogicalOpRequest<?,?> reqOp = (LogicalOpRequest<?,?>) reqPlan.getRootOperator();
			final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();
			up.addSubPattern( req.getQueryPattern() );
		}

		return createPlanWithSingleRequestOp(up, fm);
	}

	protected LogicalPlan mergeSPARQLRequestsViaJoin( final FederationMember fm,
	                                                  final SPARQLRequest req1,
	                                                  final SPARQLRequest req2 ) {
		final SPARQLGraphPattern p1 = req1.getQueryPattern();
		return mergePatternIntoRequest(p1, fm, req2);
	}

	/**
	 * Assumes that the given list contains at list two plans and that
	 * all plans in the list consist only of a request operator.
	 */
	protected LogicalPlan mergeSPARQLRequestsViaJoin( final FederationMember fm,
	                                                  final List<LogicalPlan> reqPlans ) {
		final Iterator<LogicalPlan> it = reqPlans.iterator();

		LogicalPlan plan1 = it.next();

		while ( it.hasNext() ) {
			final LogicalOpRequest<?,?> reqOp1 = (LogicalOpRequest<?,?>) plan1.getRootOperator();
			final SPARQLRequest req1 = (SPARQLRequest) reqOp1.getRequest();

			final LogicalPlan plan2 = it.next();
			final LogicalOpRequest<?,?> reqOp2 = (LogicalOpRequest<?,?>) plan2.getRootOperator();
			final SPARQLRequest req2 = (SPARQLRequest) reqOp2.getRequest();

			plan1 = mergeSPARQLRequestsViaJoin(fm, req1, req2);
		}

		return plan1;
	}

	protected LogicalPlan mergeOptPatternsIntoRequest( final List<SPARQLGraphPattern> optPatterns,
	                                                              final SPARQLEndpoint fm,
	                                                              final SPARQLRequest req ) {
		assert ! optPatterns.isEmpty();

		final ElementGroup group = new ElementGroup();

		final Element elmt = QueryPatternUtils.convertToJenaElement( req.getQueryPattern() );
		if ( elmt instanceof ElementGroup ) {
			for ( final Element subElmt : ((ElementGroup) elmt).getElements() ) {
				group.addElement(subElmt);
			}
		}
		else {
			group.addElement(elmt);
		}

		for ( final SPARQLGraphPattern optPattern : optPatterns ) {
			final Element elmtInOpt = QueryPatternUtils.convertToJenaElement(optPattern);
			final ElementOptional opt = new ElementOptional(elmtInOpt);
			group.addElement(opt);
		}

		return createPlanWithSingleRequestOp(group, fm);
	}

	protected LogicalPlan createPlanWithSingleRequestOp( final Element elmt, final SPARQLEndpoint fm ) {
		final SPARQLGraphPattern mergedPattern = new GenericSPARQLGraphPatternImpl1(elmt);
		return createPlanWithSingleRequestOp(mergedPattern, fm);
	}

	protected LogicalPlan createPlanWithSingleRequestOp( final SPARQLGraphPattern p, final SPARQLEndpoint fm ) {
		final SPARQLRequest mergedReq = new SPARQLRequestImpl(p);
		final LogicalOpRequest<SPARQLRequest,SPARQLEndpoint> mergedReqOp = new LogicalOpRequest<>(fm, mergedReq);
		return new LogicalPlanWithNullaryRootImpl(mergedReqOp);
	}

	protected void separateSubPlansOfMultiwayOps( final List<LogicalPlan> subPlans,
	                                              final Map<FederationMember,List<LogicalPlan>> reqOnlyPlansPerFedMember,
	                                              final List<LogicalPlan> nonReqSubPlans ) {
		for ( final LogicalPlan p : subPlans ) {
			final LogicalOperator rootOp = p.getRootOperator();
			if ( rootOp instanceof LogicalOpRequest<?,?> reqOp ) {
				final FederationMember fm = reqOp.getFederationMember();
				if ( fm.getInterface().supportsBGPRequests() ) {
					List<LogicalPlan> reqPlansForFM = reqOnlyPlansPerFedMember.get(fm);
					if ( reqPlansForFM == null ) {
						reqPlansForFM = new ArrayList<>();
						reqOnlyPlansPerFedMember.put(fm, reqPlansForFM);
					}
					reqPlansForFM.add(p);
				}
				else {
					nonReqSubPlans.add(p);
				}
			}
			else {
				nonReqSubPlans.add(p);
			}
		}
	}

}
