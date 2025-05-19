package se.liu.ida.hefquin.engine.queryplan.physical;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;

public interface PhysicalPlan
{
	/**
	 * Returns the root operator of this plan.
	 */
	PhysicalOperator getRootOperator();

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
	PhysicalPlan getSubPlan( int i ) throws NoSuchElementException;
}
