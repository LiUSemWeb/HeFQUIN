package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

/**
 * This class is an implementation of {@link CardinalityBasedJoinOrderingBase}
 * that uses cardinality requests. That is, this class implements the function
 * {@link CardinalityBasedJoinOrderingBase#estimateCardinalities(LogicalPlan[] plans)}
 * by, first, issuing cardinality requests for every request operator within
 * each of the given plans; then, for each given plan, the cardinalities for
 * obtained for the corresponding request operators are aggregated. Currently,
 * this aggregation is just a sum, ignoring whether there are filters or l2g
 * operators in the plans.
 *
 * The implementation of the second of the abstract functions of the base class,
 * {@link CardinalityBasedJoinOrderingBase#estimateJoinCardinality(List<AnnotatedLogicalPlan> selectedPlans, int joinCardOfSelectedPlans, AnnotatedLogicalPlan nextCandidate)},
 * simply returns the cardinality estimate of the given next candidate subplan.
 * Hence, when deciding which next candidate subplan to add to the join order,
 * this implementation greedily picks the candidate with the lowest cardinality
 * (without considering join cardinalities or anything even more sophisticated).
 * Subclasses may change this behavior.
 *
 * The current implementation of this class assumes that all logical plans under
 * joins (i.e., all plans for which the cardinality needs to be estimated) are
 * either:
 * i) a single request,
 * ii) a filter over a request,
 * iii) an l2g over a request,
 * iv) a filter over an l2g over a request,
 * v) an l2g over a filter over a request,
 * vi) a union over any of the aforementioned options, or
 * vii) an l2g over a union over any of the aforementioned (non-union) options.
 */
public class CardinalityBasedJoinOrderingWithRequests extends CardinalityBasedJoinOrderingBase
{
	protected final QueryProcContext ctxt;

	public CardinalityBasedJoinOrderingWithRequests( final QueryProcContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	protected int[] estimateCardinalities( final LogicalPlan[] plans ) throws LogicalOptimizationException {
		// Extract all request operators from the given list of plans.
		final List<LogicalOpRequest<?,?>>[] reqOpsPerPlan = extractAllRequestOps(plans);

		// Flatten the array of request operators into a single list.
		final List<LogicalOpRequest<?,?>> reqOpsOfAllPlans = new ArrayList<>();
		for ( int i = 0; i < plans.length; i++ ) {
			reqOpsOfAllPlans.addAll( reqOpsPerPlan[i] );
		}

		// Next, get cardinality estimates for all of the request
		// operators by performing cardinality requests for them.
		// Notice that we do this in a batch in order to leverage
		// parallelism when doing the cardinality requests.
		final CardinalityResponse[] resps;
		try {
			resps = FederationAccessUtils.performCardinalityRequests( ctxt.getFederationAccessMgr(),
			                                                          reqOpsOfAllPlans );
		}
		catch ( final FederationAccessException e ) {
			throw new LogicalOptimizationException("Issuing a cardinality request caused an exception.", e);
		}

		// Populate the result.
		final int[] result = new int[ plans.length ];
		int respsCounter = 0; 
		for ( int i = 0; i < plans.length; i++ ) {
			// The number of cardinality responses that are available for the
			// i-th plan is equivalent to the number of request operators that
			// were extracted from that plan.
			final int numOfReqOps = reqOpsPerPlan[i].size();
			if ( numOfReqOps == 1 ) {
				// If there was only one request operator for the i-th plan
				// and, thus, there is only one cardinality response to be
				// considered, simply use the cardinality of that cardinality
				// response for the result.
				result[i] = computeEffectiveCardinality( resps[respsCounter++] );
			}
			else {
				// Otherwise, aggregate the cardinalities of the cardinality
				// responses to be considered for the i-th plan.
				int aggregatedCardinality = 0;
				for ( int j = 0; j < numOfReqOps; j++ ) {
					// When aggregating, make sure that we do not end up with
					// a negative value because we exceeded the maximum of int.
					if ( aggregatedCardinality == Integer.MAX_VALUE ) {
						// If we are already at the maximum, just increase the counter.
						respsCounter++;
						continue;
					}

					final int c = computeEffectiveCardinality( resps[respsCounter++] );
					aggregatedCardinality += (c == Integer.MAX_VALUE) ? Integer.MAX_VALUE : c;
					if ( aggregatedCardinality < 0 ) aggregatedCardinality = Integer.MAX_VALUE;
				}

				result[i] = aggregatedCardinality;
			} 
		}

		return result;
	}

	@Override
	protected int estimateJoinCardinality( final List<AnnotatedLogicalPlan> selectedPlans,
	                                        final int joinCardOfSelectedPlans,
	                                        final AnnotatedLogicalPlan nextCandidate ) {
		return nextCandidate.cardinality;
	}


	/**
	 * Extracts all request operators from the given list of plans such
	 * that, for each of the plans, the resulting list of these operators
	 * is ordered in the order in which the operators can be found by a
	 * depth-first traversal of the plan.
	 */
	protected List<LogicalOpRequest<?,?>>[] extractAllRequestOps( final LogicalPlan[] plans ) {
		final List<?>[] result = new List<?>[ plans.length ];
		for ( int i = 0; i < plans.length; i++ ) {
			result[i] = extractAllRequestOps( plans[i] );
		}

		@SuppressWarnings("unchecked")
		final List<LogicalOpRequest<?,?>>[] result2 = (List<LogicalOpRequest<?,?>>[]) result;
		return result2;
	}

	/**
	 * Extracts all request operators from the given plan such that the
	 * resulting list of these operators will be ordered in the order in
	 * which the operators can be found by a depth-first traversal of the
	 * given plan.
	 */
	protected List<LogicalOpRequest<?,?>> extractAllRequestOps( final LogicalPlan plan ) {
		final LogicalOperator rootOp = plan.getRootOperator();

		if ( rootOp instanceof LogicalOpRequest reqOp ) {
			return Arrays.asList(reqOp);
		}
		else if ( rootOp instanceof LogicalOpFilter )
		{
			final LogicalPlan subplan = plan.getSubPlan(0);
			final LogicalOperator subrootOp = subplan.getRootOperator();
			if ( subrootOp instanceof LogicalOpRequest subReqOp ) {
				return Arrays.asList(subReqOp);
			}
			if ( subrootOp instanceof LogicalOpLocalToGlobal ) {
				return extractAllRequestOps(subplan);
			}
			else {
				throw new IllegalArgumentException("Unsupported type of subplan under filter (" + subrootOp.getClass().getName() + ")");
			}
		}
		else if ( rootOp instanceof LogicalOpBind )
		{
			return extractAllRequestOps( plan.getSubPlan(0) );
		}
		else if ( rootOp instanceof LogicalOpLocalToGlobal )
		{
			final LogicalPlan subplan = plan.getSubPlan(0);
			final LogicalOperator subrootOp = subplan.getRootOperator();
			if ( subrootOp instanceof LogicalOpRequest subReqOp ) {
				return Arrays.asList(subReqOp);
			}
			if ( subrootOp instanceof LogicalOpFilter ) {
				return extractAllRequestOps(subplan);
			}
			else {
				throw new IllegalArgumentException("Unsupported type of subplan under l2g operator (" + subrootOp.getClass().getName() + ")");
			}
		}
		else if ( rootOp instanceof LogicalOpUnion || rootOp instanceof LogicalOpMultiwayUnion )
		{
			final List<LogicalOpRequest<?,?>> result = new ArrayList<>();
			final int numOfSubPlans = plan.numberOfSubPlans();
			for ( int i = 0; i < numOfSubPlans; i++ ) {
				final LogicalPlan subPlan = plan.getSubPlan(i);
				result.addAll( extractAllRequestOps(subPlan) );
			}
			return result;
		}
		else {
			throw new IllegalArgumentException("Unsupported type of plan given (" + rootOp.getClass().getName() + ")");
		}
	}

	/**
	 * TODO: Fallback behavior? Returning Integer.MAX_VALUE for now
	 *
	 * Computes the cardinality from the given {@link CardinalityResponse}.
	 *
	 * If retrieving the cardinality fails due to an {@link UnsupportedOperationDueToRetrievalError}, this method
	 * returns {@link Integer#MAX_VALUE} as a fallback.
	 *
	 * @param resp the cardinality response to extract the cardinality from
	 * @return the cardinality, or {@code Integer.MAX_VALUE} if retrieval is unsupported
	 */
	private int computeEffectiveCardinality( final CardinalityResponse resp ) {
		try {
			return resp.getCardinality();
		} catch ( UnsupportedOperationDueToRetrievalError e ) {
			return Integer.MAX_VALUE;
		}
	}
}
