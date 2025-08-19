package se.liu.ida.hefquin.engine.queryplan.base;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

/**
 * This interface captures aspects that are common both to logical plans and
 * to physical plans. That is, every such plan has a root operator and child
 * plans that produce the input to the root operator. Moreover, every plan
 * may be associated with query-planning-related information.
 * <p>
 * This interface serves purely an abstract purpose in the sense that it is
 * not meant to be instantiated directly. Instead, {@link LogicalPlan} and
 * {@link PhysicalPlan}) are the relevant specializations of this interfaces
 * that are meant to be used throughout the code base.
 */
public interface QueryPlan
{
	/**
	 * Returns the root operator of this plan.
	 */
	QueryPlanOperator getRootOperator();

	/**
	 * Returns the number of sub-plans that this plan has
	 * (considering sub-plans that are direct children of
	 * the root operator of this plan).
	 */
	int numberOfSubPlans();

	/**
	 * Returns the i-th sub-plan of this plan, where i starts at
	 * index 0 (zero).
	 *
	 * If the plan has fewer sub-plans (or no sub-plans at all),
	 * then a {@link NoSuchElementException} will be thrown.
	 */
	QueryPlan getSubPlan( int i ) throws NoSuchElementException;

	/**
	 * Returns the variables that can be expected in the
	 * solution mappings produced by this plan.
	 */
	ExpectedVariables getExpectedVariables();

	/**
	 * Returns an object that captures query-planning-related
	 * information about this plan. This object is meant to be
	 * requested and populated by the query planner.
	 * <p>
	 * If this plan does not yet have a {@link QueryPlanningInfo}
	 * object associated with it, then this function creates a new
	 * (empty) one and returns that.
	 */
	QueryPlanningInfo getQueryPlanningInfo();

	/**
	 * Returns <code>true</code> if this plan already has a
	 * {@link QueryPlanningInfo} object associated with it.
	 */
	boolean hasQueryPlanningInfo();
}
