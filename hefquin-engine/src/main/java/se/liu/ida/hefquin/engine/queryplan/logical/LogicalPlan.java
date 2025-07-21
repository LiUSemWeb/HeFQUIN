package se.liu.ida.hefquin.engine.queryplan.logical;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

public interface LogicalPlan
{
	/**
	 * Returns the root operator of this plan.
	 */
	LogicalOperator getRootOperator();

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
	LogicalPlan getSubPlan( int i ) throws NoSuchElementException;

	/**
	 * Returns an object that captures query-planning-related
	 * information about this plan. This object is meant to be
	 * requested and populated by the query planner. 
	 */
	QueryPlanningInfo getQueryPlanningInfo();
}
