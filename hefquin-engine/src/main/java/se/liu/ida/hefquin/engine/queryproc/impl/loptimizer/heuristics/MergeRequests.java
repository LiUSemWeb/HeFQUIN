package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;

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
 * operators but also for gpAdd operators over request operators, as well as
 * for pairs of requests under a multi-join. In the latter case, the multi-join
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
		else if ( rootOp instanceof LogicalOpGPAdd gpAdd )
		{
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if (    childOp instanceof LogicalOpRequest reqOp
			     && reqOp.getRequest() instanceof SPARQLRequest req
			     && reqOp.getFederationMember().supportsMoreThanTriplePatterns()
			     && reqOp.getFederationMember().equals(gpAdd.getFederationMember())
			     && ! gpAdd.hasParameterVariables() )
			{
				final SPARQLGraphPattern pattern1 = gpAdd.getPattern();
				final SPARQLGraphPattern pattern2 = req.getQueryPattern();
				final SPARQLGraphPattern mergedPattern = pattern1.mergeWith(pattern2);

				final FederationMember fm = gpAdd.getFederationMember();
				if ( fm.isSupportedPattern(mergedPattern) ) {
					return createPlanWithSingleRequestOp(mergedPattern, fm);
				}
			}
		}
		else if ( rootOp instanceof LogicalOpGPOptAdd gpOptAdd )
		{
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if (    childOp instanceof LogicalOpRequest reqOp
			     && reqOp.getRequest() instanceof SPARQLRequest req
			     && reqOp.getFederationMember().supportsMoreThanTriplePatterns()
			     && reqOp.getFederationMember().equals(gpOptAdd.getFederationMember()) )
			{
				final FederationMember fm = gpOptAdd.getFederationMember();
				final SPARQLGraphPattern merged = mergePatternWithOptPatterns( req.getQueryPattern(),
				                                                               gpOptAdd.getPattern() );

				if ( fm.isSupportedPattern(merged) ) {
					return createPlanWithSingleRequestOp(merged, fm);
				}
			}
		}
		else if ( rootOp instanceof LogicalOpFilter filterOp )
		{
			// A filter can be merged into a request operator if that request
			// is for a SPARQL endpoint.
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if (    childOp instanceof LogicalOpRequest reqOp
			     && reqOp.getRequest() instanceof SPARQLRequest req
			     && reqOp.getFederationMember().supportsMoreThanTriplePatterns() )
			{
				final ExprList exprList = filterOp.getFilterExpressions();
				final SPARQLGraphPattern reqPattern = req.getQueryPattern();
				final SPARQLGraphPattern mergedPattern = reqPattern.mergeWith(exprList);

				final FederationMember fm = reqOp.getFederationMember();
				if ( fm.isSupportedPattern(mergedPattern) ) {
					return createPlanWithSingleRequestOp(mergedPattern, fm);
				}
			}
		}
		else if ( rootOp instanceof LogicalOpBind )
		{
			// nothing to do here - while the BIND clause can be merged into
			// a request operator if that request is for a SPARQL endpoint,
			// unlike FILTER, for BIND this only increases the size of the solution
			// mappings returned from the endpoint. The optimizer should instead
			// retain the BIND outside the request.
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
			     && childOp2 instanceof LogicalOpRequest reqOp2
			     && reqOp1.getRequest() instanceof SPARQLRequest req1
			     && reqOp2.getRequest() instanceof SPARQLRequest req2
			     && reqOp1.getFederationMember().supportsMoreThanTriplePatterns()
			     && reqOp1.getFederationMember().equals(reqOp2.getFederationMember()) )
			{
				final FederationMember fm = reqOp1.getFederationMember();

				final SPARQLGraphPattern p1 = req1.getQueryPattern();
				final SPARQLGraphPattern p2 = req2.getQueryPattern();
				final SPARQLGraphPattern mergedPattern = new SPARQLUnionPatternImpl(p1, p2);

				if ( fm.isSupportedPattern(mergedPattern) ) {
					return createPlanWithSingleRequestOp(mergedPattern, fm);
				}
			}
		}
		else if ( rootOp instanceof LogicalOpJoin )
		{
			final LogicalOperator childOp1 = rewrittenSubPlans.get(0).getRootOperator();
			final LogicalOperator childOp2 = rewrittenSubPlans.get(1).getRootOperator();
			if (    childOp1 instanceof LogicalOpRequest reqOp1
			     && childOp2 instanceof LogicalOpRequest reqOp2
			     && reqOp1.getRequest() instanceof SPARQLRequest req1
			     && reqOp2.getRequest() instanceof SPARQLRequest req2
			     && reqOp1.getFederationMember().supportsMoreThanTriplePatterns()
			     && reqOp1.getFederationMember().equals(reqOp2.getFederationMember()) )
			{
				final FederationMember fm = reqOp1.getFederationMember();

				final SPARQLGraphPattern p1 = req1.getQueryPattern();
				final SPARQLGraphPattern p2 = req2.getQueryPattern();
				final SPARQLGraphPattern mergedPattern = p1.mergeWith(p2);

				if ( fm.isSupportedPattern(mergedPattern) ) {
					return createPlanWithSingleRequestOp(mergedPattern, fm);
				}
			}
		}
		else if ( rootOp instanceof LogicalOpRightJoin )
		{
			final LogicalOperator childOp1 = rewrittenSubPlans.get(0).getRootOperator();
			final LogicalOperator childOp2 = rewrittenSubPlans.get(1).getRootOperator();
			if (    childOp1 instanceof LogicalOpRequest reqOp1
			     && childOp2 instanceof LogicalOpRequest reqOp2
			     && reqOp1.getRequest() instanceof SPARQLRequest req1
			     && reqOp2.getRequest() instanceof SPARQLRequest req2
			     && reqOp1.getFederationMember().equals(reqOp2.getFederationMember()) )
			{
				// the LHS is the optional part
				final SPARQLGraphPattern merged = mergePatternWithOptPatterns( req2.getQueryPattern(),
				                                                               req1.getQueryPattern() );

				final FederationMember fm = reqOp1.getFederationMember();
				if ( fm.isSupportedPattern(merged) ) {
					return createPlanWithSingleRequestOp(merged, fm);
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
				final List<LogicalPlan> reqPlans = e.getValue();
				if ( reqPlans.size() > 1 ) {
					final SPARQLGraphPattern mergedPattern = mergeSPARQLRequestsViaUnion(reqPlans);
					final FederationMember fm = e.getKey();
					if ( fm.isSupportedPattern(mergedPattern) ) {
						final LogicalPlan mergedSubPlan = createPlanWithSingleRequestOp(mergedPattern, fm);
						newSubPlans.add(mergedSubPlan);
						noChange = false;
					}
					else {
						newSubPlans.addAll(reqPlans);
					}
				}
				else {
					newSubPlans.addAll(reqPlans);
				}
			}

			if ( noChange == false ) {
				if ( newSubPlans.size() == 1 )
					return newSubPlans.get(0);
				else
					return LogicalPlanUtils.createPlanWithSubPlans( rootOp,
					                                                null,
					                                                newSubPlans );
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
				final List<LogicalPlan> reqPlans = e.getValue();
				if ( reqPlans.size() > 1 ) {
					final FederationMember fm = e.getKey();
					final LogicalPlan mergedSubPlan = mergeSPARQLRequestsViaJoin(fm, reqPlans);
					if ( mergedSubPlan != null ) {
						newSubPlans.add(mergedSubPlan);
						noChange = false;
					}
					else {
						newSubPlans.addAll(reqPlans);
					}
				}
				else {
					newSubPlans.addAll(reqPlans);
				}
			}

			if ( noChange == false ) {
				if ( newSubPlans.size() == 1 )
					return newSubPlans.get(0);
				else
					return LogicalPlanUtils.createPlanWithSubPlans( rootOp,
					                                                null,
					                                                newSubPlans );
			}
		}
		else if ( rootOp instanceof LogicalOpMultiwayLeftJoin )
		{
			// ignore - If the non-optional subplan is just a request with a
			// SPARQL endpoint as federation member, then it is possible to
			// collect all optional subplans that are also only requests for
			// the same SPARQL endpoint and merge them as optional parts into
			// the non-optional request; the other optional subplans (if any)
			// need to be kept as optional subplans. But implement this only
			// if we really need it.
		}
		else
		{
			throw new IllegalArgumentException( "unexpected type of logical operator: " + rootOp.getClass().getName() );
		}

		// Finally, if the heuristic was not applied to the root of
		// the plan, return the plan without changing its root, but
		// make sure to use the rewritten subplans if necessary.
		if ( subPlansDiffer )
			return LogicalPlanUtils.createPlanWithSubPlans( rootOp,
			                                                null,
			                                                rewrittenSubPlans );
		else
			return inputPlan;
	}

	/**
	 * Assumes that the given list contains at least two plans and that
	 * all plans in the list consist only of a request operator.
	 */
	protected SPARQLGraphPattern mergeSPARQLRequestsViaUnion( final List<LogicalPlan> reqPlans ) {
		final SPARQLUnionPatternImpl up = new SPARQLUnionPatternImpl();
		for ( final LogicalPlan reqPlan : reqPlans ) {
			final LogicalOpRequest<?,?> reqOp = (LogicalOpRequest<?,?>) reqPlan.getRootOperator();
			final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();
			up.addSubPattern( req.getQueryPattern() );
		}

		return up;
	}

	/**
	 * Assumes that the given list contains at least two plans and that
	 * all plans in the list consist only of a request operator. Returns
	 * {@code null} if the given federation member does not support the
	 * merged pattern.
	 */
	protected LogicalPlan mergeSPARQLRequestsViaJoin( final FederationMember fm,
	                                                  final List<LogicalPlan> reqPlans ) {
		final Iterator<LogicalPlan> it = reqPlans.iterator();

		final LogicalPlan plan1 = it.next();
		final LogicalOpRequest<?,?> reqOp1 = (LogicalOpRequest<?,?>) plan1.getRootOperator();
		final SPARQLRequest req1 = (SPARQLRequest) reqOp1.getRequest();
		final SPARQLGraphPattern pattern1 = req1.getQueryPattern();

		final LogicalPlan plan2 = it.next();
		final LogicalOpRequest<?,?> reqOp2 = (LogicalOpRequest<?,?>) plan2.getRootOperator();
		final SPARQLRequest req2 = (SPARQLRequest) reqOp2.getRequest();
		final SPARQLGraphPattern pattern2 = req2.getQueryPattern();

		SPARQLGraphPattern mergedPattern = pattern1.mergeWith(pattern2);

		// Do a first check of the merged pattern already at this point
		// to avoid going into the following loop if there is no need
		// for it anyways.
		if ( ! fm.isSupportedPattern(mergedPattern) )
			return null;

		// If there are no more subplans to consider, we can already
		// return the plan with the merged pattern now (otherwise, we
		// would end up repeating the check of the same merged pattern
		// again after after the loop).
		if ( ! it.hasNext() )
			return createPlanWithSingleRequestOp(mergedPattern, fm);

		while ( it.hasNext() ) {
			final LogicalPlan nextPlan = it.next();
			final LogicalOpRequest<?,?> nextReqOp = (LogicalOpRequest<?,?>) nextPlan.getRootOperator();
			final SPARQLRequest nextReq = (SPARQLRequest) nextReqOp.getRequest();
			final SPARQLGraphPattern nextPattern = nextReq.getQueryPattern();

			mergedPattern = mergedPattern.mergeWith(nextPattern);
		}

		// Now we need to do another check of the final merged pattern.
		if ( ! fm.isSupportedPattern(mergedPattern) )
			return null;

		return createPlanWithSingleRequestOp(mergedPattern, fm);
	}

	protected SPARQLGraphPattern mergePatternWithOptPatterns( final SPARQLGraphPattern pattern,
	                                                          final SPARQLGraphPattern ... optPatterns ) {
		assert optPatterns.length > 0;

		final ElementGroup group = new ElementGroup();

		final Element elmt = QueryPatternUtils.convertToJenaElement(pattern);
		if ( elmt instanceof ElementGroup ) {
			for ( final Element subElmt : ((ElementGroup) elmt).getElements() ) {
				group.addElement(subElmt);
			}
		}
		else {
			group.addElement(elmt);
		}

		for ( int i = 0; i < optPatterns.length; i++ ) {
			final Element elmtInOpt = QueryPatternUtils.convertToJenaElement( optPatterns[i] );
			final ElementOptional opt = new ElementOptional(elmtInOpt);
			group.addElement(opt);
		}

		return new GenericSPARQLGraphPatternImpl1(group);
	}

	protected LogicalPlan createPlanWithSingleRequestOp( final SPARQLGraphPattern p,
	                                                     final FederationMember fm ) {
		final SPARQLRequest req;
		if ( p instanceof TriplePattern tp ) {
			req = new TriplePatternRequestImpl(tp);
		}
		else if ( p instanceof BGP bgp ) {
			req = new BGPRequestImpl(bgp);
		}
		else {
			req = new SPARQLRequestImpl(p);
		}

		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(fm, req);
		return new LogicalPlanWithNullaryRootImpl(reqOp, null);
	}

	protected void separateSubPlansOfMultiwayOps( final List<LogicalPlan> subPlans,
	                                              final Map<FederationMember,List<LogicalPlan>> reqOnlyPlansPerFedMember,
	                                              final List<LogicalPlan> nonReqSubPlans ) {
		for ( final LogicalPlan p : subPlans ) {
			final LogicalOperator rootOp = p.getRootOperator();
			if ( rootOp instanceof LogicalOpRequest<?,?> reqOp ) {
				final FederationMember fm = reqOp.getFederationMember();
				if ( fm.supportsMoreThanTriplePatterns() ) {
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
