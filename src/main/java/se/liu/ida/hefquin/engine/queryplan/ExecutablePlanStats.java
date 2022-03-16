package se.liu.ida.hefquin.engine.queryplan;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.utils.Stats;

/**
 * This interface captures statistics collected during
 * the execution of an {@link ExecutablePlan}.
 */
public interface ExecutablePlanStats extends Stats
{
	/**
	 * Returns the stats of the root operator of the executable
	 * plan for which this object has been created.
	 */
	ExecutableOperatorStats getRootOperatorStats();

	/**
	 * Returns the number of sub-plans of the executable plan
	 * for which this object has been created (considering
	 * sub-plans that are direct children of the root operator
	 * of the plan).
	 */
	int getNumberOfSubPlans();

	/**
	 * Returns the stats created for the i-th sub-plan of the
	 * executable plan for which this object has been created,
	 * where i starts at index 0 (zero).
	 *
	 * If the plan had fewer sub-plans (or no sub-plans at all),
	 * then a {@link NoSuchElementException} will be thrown.
	 */
	ExecutablePlanStats getSubPlanStats( int i ) throws NoSuchElementException;

	/**
	 * Returns an iterable over the stats created for the sub-plans of
	 * the executable plan for which this object has been created.
	 */
	Iterable<ExecutablePlanStats> getSubPlanStats();
}
