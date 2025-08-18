package se.liu.ida.hefquin.engine.queryplan.info;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;

/**
 * This interface captures functionality that is common to both every
 * logical plan and every physical plan.
 */
public interface GenericPlan
{
	/**
	 * Returns the variables that can be expected in the
	 * solution mappings produced by this plan.
	 */
	ExpectedVariables getExpectedVariables();

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
	GenericPlan getSubPlan( int i ) throws NoSuchElementException;

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
