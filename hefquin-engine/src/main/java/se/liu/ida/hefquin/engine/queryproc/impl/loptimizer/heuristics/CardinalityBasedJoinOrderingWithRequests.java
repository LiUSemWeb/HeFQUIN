package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

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
// TODO: The Javadoc comment is outdated. See the base class.
public class CardinalityBasedJoinOrderingWithRequests extends CardinalityBasedJoinOrderingBase
{
	protected final QueryProcContext ctxt;

	public CardinalityBasedJoinOrderingWithRequests( final QueryProcContext ctxt ) {
		super(ctxt);

		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	protected int estimateJoinCardinality( final List<LogicalPlan> selectedPlans,
	                                       final int joinCardOfSelectedPlans,
	                                       final LogicalPlan nextCandidate ) {
		return nextCandidate.getQueryPlanningInfo().getProperty(QueryPlanProperty.CARDINALITY).getValue();
	}

}
