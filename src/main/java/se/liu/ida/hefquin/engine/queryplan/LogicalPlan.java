package se.liu.ida.hefquin.engine.queryplan;

import java.util.NoSuchElementException;

public interface LogicalPlan
{
	/**
	 * Returns the root operator of this plan.
	 */
	LogicalOperator getRootOperator();

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
}
