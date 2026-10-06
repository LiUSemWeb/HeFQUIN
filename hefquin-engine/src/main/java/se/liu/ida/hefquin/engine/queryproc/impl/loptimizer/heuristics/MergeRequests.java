package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementOptional;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

/**
 * Merges subplans that consist of multiple requests to the same federation
 * member if such a merge is possible. Merging of requests is avoided for
 * cases in which the graph pattern of the merged request consists of two
 * unconnected sub-patterns (because that would lead to retrieving the
 * cross product of the results of these two sub-patterns).
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
	public LogicalPlan apply( final LogicalPlan inputPlan,
	                          final QueryProcContext ctxt2 ) {
		return apply(inputPlan);
	}

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
		final boolean mayReduce = rootOp.mayReduce();

		final Worker worker = new Worker(rewrittenSubPlans, mayReduce);
		rootOp.visit(worker);

		final LogicalPlan rewrittenPlan = worker.getRewrittenPlan();
		if ( rewrittenPlan != null ) return rewrittenPlan;

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

	protected class Worker implements LogicalPlanVisitor {
		protected final List<LogicalPlan> rewrittenSubPlans;
		protected final boolean mayReduce;
		protected LogicalPlan returnPlan;

		public Worker( final List<LogicalPlan> rewrittenSubPlans, final boolean mayReduce ) {
			this.rewrittenSubPlans = rewrittenSubPlans;
			this.mayReduce = mayReduce;
		}

		public LogicalPlan getRewrittenPlan() { return returnPlan; }

		@Override
		public void visit( final LogicalOpRequest<?, ?> op ) {
			// nothing to do here - we are in a leaf node
		}

		@Override
		public void visit( final LogicalOpMultiRequest op ) {
			// nothing to do here - we are in a leaf node
		}

		@Override
		public void visit( final LogicalOpFixedSolMap op ) {
			// nothing to do here - we are in a leaf node
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if (    childOp instanceof LogicalOpRequest reqOp
					&& reqOp.getRequest() instanceof SPARQLRequest req
					&& reqOp.getFederationMember().supportsMoreThanTriplePatterns()
					&& reqOp.getFederationMember().equals(op.getFederationMember())
					&& ! op.hasParameterVariables() )
			{
				final SPARQLGraphPattern pattern1 = op.getPattern();
				final SPARQLGraphPattern pattern2 = req.getQueryPattern();

				// Avoid merging requests if this leads to a cross product
				if ( ! containsJoinVariable( pattern1, pattern2 ) )
					return;

				final SPARQLGraphPattern mergedPattern = pattern1.mergeWith(pattern2);

				final FederationMember fm = op.getFederationMember();
				if ( fm.isSupportedPattern(mergedPattern) ) {
					returnPlan = createPlanWithSingleRequestOp(mergedPattern, mayReduce, fm);
				}
			}
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if (    childOp instanceof LogicalOpRequest reqOp
					&& reqOp.getRequest() instanceof SPARQLRequest req
					&& reqOp.getFederationMember().supportsMoreThanTriplePatterns()
					&& reqOp.getFederationMember().equals(op.getFederationMember()) )
			{
				final FederationMember fm = op.getFederationMember();
				final SPARQLGraphPattern pattern1 = req.getQueryPattern();
				final SPARQLGraphPattern pattern2 = op.getPattern();

				// Avoid merging requests if this leads to a cross product
				if ( ! containsJoinVariable( pattern1, pattern2 ) )
					return;

				final SPARQLGraphPattern merged = mergePatternWithOptPatterns(pattern1, pattern2);

				if ( fm.isSupportedPattern(merged) ) {
					returnPlan = createPlanWithSingleRequestOp(merged, mayReduce, fm);
				}
			}
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
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

				final SPARQLGraphPattern pattern1 = req1.getQueryPattern();
				final SPARQLGraphPattern pattern2 = req2.getQueryPattern();

				// Avoid merging requests if this leads to a cross product
				if ( ! containsJoinVariable( pattern1, pattern2 ) )
					return;

				final SPARQLGraphPattern mergedPattern = pattern1.mergeWith(pattern2);

				if ( fm.isSupportedPattern(mergedPattern) ) {
					returnPlan = createPlanWithSingleRequestOp(mergedPattern, mayReduce, fm);
				}
			}
		}

		@Override
		public void visit( final LogicalOpLeftJoin op ) {
			final LogicalOperator childOp1 = rewrittenSubPlans.get(0).getRootOperator();
			final LogicalOperator childOp2 = rewrittenSubPlans.get(1).getRootOperator();
			if (    childOp1 instanceof LogicalOpRequest reqOp1
					&& childOp2 instanceof LogicalOpRequest reqOp2
					&& reqOp1.getRequest() instanceof SPARQLRequest req1
					&& reqOp2.getRequest() instanceof SPARQLRequest req2
					&& reqOp1.getFederationMember().equals(reqOp2.getFederationMember()) )
			{
				final SPARQLGraphPattern pattern1 = req1.getQueryPattern();
				final SPARQLGraphPattern pattern2 = req2.getQueryPattern();

				// Avoid merging requests if this leads to a cross product
				if ( ! containsJoinVariable( pattern1, pattern2 ) )
					return;

				// the LHS is the non-optional part
				final SPARQLGraphPattern merged = mergePatternWithOptPatterns(pattern1, pattern2);

				final FederationMember fm = reqOp1.getFederationMember();
				if ( fm.isSupportedPattern(merged) ) {
					returnPlan = createPlanWithSingleRequestOp(merged, mayReduce, fm);
				}
			}
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
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
					returnPlan = createPlanWithSingleRequestOp(mergedPattern, mayReduce, fm);
				}
			}
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			assert rewrittenSubPlans.size() > 0;
			if ( rewrittenSubPlans.size() == 1 ) {
				returnPlan = rewrittenSubPlans.get(0);
				return;
			}

			final List<LogicalPlan> newSubPlans = new ArrayList<>(rewrittenSubPlans.size());
			final Map<FederationMember,List<LogicalPlan>> reqOnlyPlansPerFedMember = new HashMap<>();

			separateSubPlansOfMultiwayOps(rewrittenSubPlans, reqOnlyPlansPerFedMember, newSubPlans);

			boolean noChange = true;
			for ( final Map.Entry<FederationMember,List<LogicalPlan>> e : reqOnlyPlansPerFedMember.entrySet() ) {
				final List<LogicalPlan> reqPlans = e.getValue();
				if ( reqPlans.size() > 1 ) {
					final FederationMember fm = e.getKey();
					final List<LogicalPlan> mergedSubPlans = mergeSPARQLRequestsViaJoin(fm, mayReduce, reqPlans);
					if ( mergedSubPlans != null ) {
						newSubPlans.addAll(mergedSubPlans);
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
					returnPlan = newSubPlans.get(0);
				else
					returnPlan = LogicalPlanUtils.createPlanWithSubPlans( op,
					                                                      null,
					                                                      newSubPlans );
			}
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			// ignore - If the non-optional subplan is just a request with a
			// SPARQL endpoint as federation member, then it is possible to
			// collect all optional subplans that are also only requests for
			// the same SPARQL endpoint and merge them as optional parts into
			// the non-optional request; the other optional subplans (if any)
			// need to be kept as optional subplans. But implement this only
			// if we really need it.
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			assert rewrittenSubPlans.size() > 0;
			if ( rewrittenSubPlans.size() == 1 ) {
				returnPlan = rewrittenSubPlans.get(0);
				return;
			}

			final List<LogicalPlan> newSubPlans = new ArrayList<>(rewrittenSubPlans.size());
			final Map<FederationMember,List<LogicalPlan>> reqOnlyPlansPerFedMember = new HashMap<>();

			separateSubPlansOfMultiwayOps(rewrittenSubPlans, reqOnlyPlansPerFedMember, newSubPlans);

			boolean noChange = true;
			for ( final Map.Entry<FederationMember,List<LogicalPlan>> e : reqOnlyPlansPerFedMember.entrySet() ) {
				final List<LogicalPlan> reqPlans = e.getValue();
				if ( reqPlans.size() > 1 ) {
					final SPARQLGraphPattern mergedPattern = mergeSPARQLRequestsViaUnion(reqPlans);
					final FederationMember fm = e.getKey();
					if ( fm.isSupportedPattern(mergedPattern) ) {
						final LogicalPlan mergedSubPlan = createPlanWithSingleRequestOp(mergedPattern, mayReduce, fm);
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
					returnPlan = newSubPlans.get(0);
				else
					returnPlan = LogicalPlanUtils.createPlanWithSubPlans( op,
																	null,
																	newSubPlans );
			}
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			// A filter can be merged into a request operator if that request
			// is for a SPARQL endpoint.
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if (    childOp instanceof LogicalOpRequest reqOp
					&& reqOp.getRequest() instanceof SPARQLRequest req
					&& reqOp.getFederationMember().supportsMoreThanTriplePatterns() )
			{
				final ExprList exprList = op.getFilterExpressions();
				final SPARQLGraphPattern reqPattern = req.getQueryPattern();
				final SPARQLGraphPattern mergedPattern = reqPattern.mergeWith(exprList);

				final FederationMember fm = reqOp.getFederationMember();
				if ( fm.isSupportedPattern(mergedPattern) ) {
					returnPlan = createPlanWithSingleRequestOp(mergedPattern, mayReduce, fm);
				}
			}
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			// nothing to do here - while the BIND clause can be merged into
			// a request operator if that request is for a SPARQL endpoint,
			// unlike FILTER, for BIND this only increases the size of the solution
			// mappings returned from the endpoint. The optimizer should instead
			// retain the BIND outside the request.
		}

		@Override
		public void visit( final LogicalOpUnfold op ) {
			// nothing to do here - for the time being, an UNFOLD clause
			// should not be merged into a request operator, not even for
			// requests to a SPARQL endpoint, because it is unlikely that
			// SPARQL endpoints already support the SPARQL-CDTs approach.
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			// nothing to do here - if we have a vocabulary translation as root
			// operator, we do not attempt to merge it with its input operator
			// (unless that is another vocabulary translation, but that case is
			// covered by another rewriting rule)
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			// nothing to do here - if we have a vocabulary translation as root
			// operator, we do not attempt to merge it with its input operator
			// (unless that is another vocabulary translation, but that case is
			// covered by another rewriting rule)
		}

		@Override
		public void visit( final LogicalOpDedup op ) {
			// nothing to do here - TODO: for requests to SPARQL endpoints, the DISTINCT
			// can be merged into the request.
		}

		@Override
		public void visit( final LogicalOpProject op ) {
			// A project can be merged into a request operator if that request
			// is for a SPARQL endpoint.
			final LogicalOperator childOp = rewrittenSubPlans.get(0).getRootOperator();
			if (    childOp instanceof LogicalOpRequest reqOp
					&& reqOp.getFederationMember() instanceof SPARQLEndpoint
					&& reqOp.getRequest() instanceof SPARQLRequest req )
			{
				final Set<Var> newProj;
				if ( req.getProjectionVars() != null ) {
					newProj = new HashSet<>( op.getVariables() );
					newProj.retainAll( req.getProjectionVars() );
				}
				else {
					newProj = op.getVariables();
				}

				final boolean mayReduce = reqOp.mayReduce() && op.mayReduce();

				final SPARQLRequest newReq = new SPARQLRequestImpl( req.getQueryPattern(), newProj, req.getDistinctRequired() || mayReduce );

				final LogicalOpRequest<?,?> mergedReqOp = new LogicalOpRequest<>( reqOp.getFederationMember(), mayReduce, newReq );
				returnPlan = new LogicalPlanWithNullaryRootImpl(mergedReqOp, null);
			}
		}

		@Override
		public void visit( final LogicalOpMinus op ) {
			final LogicalOperator childOp1 = rewrittenSubPlans.get(0).getRootOperator();
			final LogicalOperator childOp2 = rewrittenSubPlans.get(1).getRootOperator();
			if (   childOp1 instanceof LogicalOpRequest reqOp1
				&& childOp2 instanceof LogicalOpRequest reqOp2
				&& reqOp1.getRequest() instanceof SPARQLRequest req1
				&& reqOp2.getRequest() instanceof SPARQLRequest req2
				&& reqOp1.getFederationMember().equals(reqOp2.getFederationMember()) )
			{
				final SPARQLGraphPattern pattern1 = req1.getQueryPattern();
				final SPARQLGraphPattern pattern2 = req2.getQueryPattern();

				// Only merge if the patterns share variables. Without shared variables,
				// MINUS has no filtering effect, so merging the requests is unnecessary.
				if ( ! containsJoinVariable( pattern1, pattern2 ) )
					return;

				// the LHS is the non-optional part
				final SPARQLGraphPattern merged = mergePatternWithMinusPatterns(pattern1, pattern2);

				final FederationMember fm = reqOp1.getFederationMember();
				if ( fm.isSupportedPattern(merged) ) {
					returnPlan = createPlanWithSingleRequestOp(merged, mayReduce, fm);
				}
			}
		}

	} // end of Worker

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
	 * Merges request plans whose patterns share variables. Patterns that
	 * are connected through shared variables are merged when the resulting
	 * pattern is supported by the federation member.
	 *
	 * Assumes that all plans in the list consist only of a request operator
	 * and that all of these request operators are for the federation member
	 * given as the first argument of this method.
	 *
	 * @param fm        the target federation member
	 * @param mayReduce whether the resulting request plans may be reduced
	 * @param reqPlans  the request plans to merge
	 * @return the resulting request plans, with joinable patterns merged, or
	 *         {@code null} if no merging was possible
	 */
	protected List<LogicalPlan> mergeSPARQLRequestsViaJoin( final FederationMember fm,
	                                                        final boolean mayReduce,
	                                                        final List<LogicalPlan> reqPlans ) {
		final List<LogicalPlan> mergedPlans = new ArrayList<>();
		final List<LogicalPlan> remaining = new ArrayList<>(reqPlans);
		boolean noChange = true;

		while ( ! remaining.isEmpty() ) {
			final LogicalPlan currentPlan = remaining.remove(0);
			if ( remaining.size() == 0 ) {
				mergedPlans.add(currentPlan);
			} else {
				final LogicalOpRequest<?,?> currentReqOp = (LogicalOpRequest<?,?>) currentPlan.getRootOperator();
				final SPARQLRequest currentReq = (SPARQLRequest) currentReqOp.getRequest();
				SPARQLGraphPattern mergedPattern = currentReq.getQueryPattern();
				boolean noMergeForCurrentPlan = true;

				for ( int i = 0; i < remaining.size(); ) {
					final LogicalPlan nextPlan = remaining.get(i);
					final LogicalOpRequest<?, ?> nextReqOp = (LogicalOpRequest<?, ?>) nextPlan.getRootOperator();
					final SPARQLRequest nextReq = (SPARQLRequest) nextReqOp.getRequest();
					final SPARQLGraphPattern nextPattern = nextReq.getQueryPattern();

					if ( containsJoinVariable(mergedPattern, nextPattern) ) {
						final SPARQLGraphPattern candidatePattern = mergedPattern.mergeWith(nextPattern);
						// Keep the merged pattern if it is supported by the federation member
						if ( fm.isSupportedPattern(candidatePattern) ) {
							mergedPattern = candidatePattern;
							remaining.remove(i);
							noChange = false;
							noMergeForCurrentPlan = false;
						} else {
							i++;
						}
					} else {
						i++;
					}
				}
				if( noMergeForCurrentPlan ) {
					mergedPlans.add(currentPlan);
				} else {
					mergedPlans.add( createPlanWithSingleRequestOp(mergedPattern, mayReduce, fm) );
				}
			}
		}

		if ( noChange ) {
			return null;
		}

		return mergedPlans;
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

	protected SPARQLGraphPattern mergePatternWithMinusPatterns( final SPARQLGraphPattern pattern,
	                                                            final SPARQLGraphPattern ... minusPatterns ) {
		assert minusPatterns.length > 0;

		final ElementGroup group = new ElementGroup();

		final Element elmt = QueryPatternUtils.convertToJenaElement(pattern);
		if ( elmt instanceof ElementGroup eg ) {
			for ( final Element subElmt : eg.getElements() ) {
				group.addElement(subElmt);
			}
		}
		else {
			group.addElement(elmt);
		}

		for ( int i = 0; i < minusPatterns.length; i++ ) {
			final Element elmtInMinus = QueryPatternUtils.convertToJenaElement( minusPatterns[i] );
			final ElementMinus minus = new ElementMinus(elmtInMinus);
			group.addElement(minus);
		}

		return new GenericSPARQLGraphPatternImpl1(group);
	}

	protected LogicalPlan createPlanWithSingleRequestOp( final SPARQLGraphPattern p,
	                                                     final boolean mayReduce,
	                                                     final FederationMember fm ) {
		final SPARQLRequest req;
		if ( p instanceof TriplePattern tp ) {
			req = new TriplePatternRequestImpl(tp);
		}
		else if ( p instanceof BGP bgp ) {
			req = new BGPRequestImpl(bgp);
		}
		else {
			req = new SPARQLRequestImpl(p, null, mayReduce);
		}

		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(fm, mayReduce, req);
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

	/**
	 * Checks whether the expected variables of the two patterns have at least one
	 * variable in common.
	 * 
	 * @param pattern1 the first SPARQL graph pattern
	 * @param pattern2 the second SPARQL graph pattern
	 * @return true if the patterns share at least one expected variable, false
	 *         otherwise
	 */
	protected boolean containsJoinVariable( final SPARQLGraphPattern pattern1, final SPARQLGraphPattern pattern2 ) {
		final ExpectedVariables vars1 = pattern1.getExpectedVariables();
		final ExpectedVariables vars2 = pattern2.getExpectedVariables();
		return ! ExpectedVariablesUtils.intersectionOfAllVariables(vars1, vars2).isEmpty();
	}
}
