package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.utils.StatsImpl;

/**
 * An implementation of {@link ExecutablePlanStats}
 * for {@link IteratorBasedExecutablePlanImpl}.
 */
public class ExecutablePlanStatsOfIteratorBasedPlan extends StatsImpl implements ExecutablePlanStats
{
	protected static final String enRootOperatorStats  = "rootOperatorStats";
	protected static final String enNumberOfSubPlans   = "numberOfSubPlans";
	protected static final String enSubPlanStats       = "subPlanStats";

	public ExecutablePlanStatsOfIteratorBasedPlan( final ExecutableOperatorStats rootOpStats, final ExecutablePlanStats ... subPlanStats ) {
		put( enRootOperatorStats,  rootOpStats );
		put( enNumberOfSubPlans,   Integer.valueOf(subPlanStats.length) );
		put( enSubPlanStats,       Arrays.asList(subPlanStats) );
	}

	/**
	 * Returns the stats of the root operator of the executable
	 * plan for which this object has been created.
	 */
	public ExecutableOperatorStats getRootOperatorStats() {
		return (ExecutableOperatorStats) getEntry(enRootOperatorStats);
	}

	/**
	 * Returns the number of sub-plans of the executable plan
	 * for which this object has been created (considering
	 * sub-plans that are direct children of the root operator
	 * of the plan).
	 */
	public int getNumberOfSubPlans() {
		return (Integer) getEntry(enNumberOfSubPlans);
	}

	/**
	 * Returns the stats created for the i-th sub-plan of the
	 * executable plan for which this object has been created,
	 * where i starts at index 0 (zero).
	 *
	 * If the plan had fewer sub-plans (or no sub-plans at all),
	 * then a {@link NoSuchElementException} will be thrown.
	 */
	public ExecutablePlanStats getSubPlanStats( final int i ) throws NoSuchElementException {
		final List<?> subPlanStats = (List<?>) getEntry(enSubPlanStats);
		if ( i >= 0 && i < subPlanStats.size() ) {
			return (ExecutablePlanStats) subPlanStats.get(i);
		}

		throw new NoSuchElementException( "i: " + i + "; size of subPlanStats: " + subPlanStats.size() );
	}

	/**
	 * Returns an iterable over the stats created for the sub-plans of
	 * the executable plan for which this object has been created.
	 */
	@SuppressWarnings("unchecked")
	public Iterable<ExecutablePlanStats> getSubPlanStats() {
		return (List<ExecutablePlanStats>) getEntry(enSubPlanStats);
	}
}
