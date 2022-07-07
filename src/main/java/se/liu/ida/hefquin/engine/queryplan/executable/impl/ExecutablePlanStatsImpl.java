package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.utils.StatsImpl;

public class ExecutablePlanStatsImpl extends StatsImpl implements ExecutablePlanStats
{
	protected static final String enRootOperatorStats  = "rootOperatorStats";
	protected static final String enNumberOfSubPlans   = "numberOfSubPlans";
	protected static final String enSubPlanStats       = "subPlanStats";

	public ExecutablePlanStatsImpl( final ExecutableOperatorStats rootOpStats, final ExecutablePlanStats ... subPlanStats ) {
		put( enRootOperatorStats,  rootOpStats );
		put( enNumberOfSubPlans,   Integer.valueOf(subPlanStats.length) );
		put( enSubPlanStats,       Arrays.asList(subPlanStats) );
	}

	@Override
	public ExecutableOperatorStats getRootOperatorStats() {
		return (ExecutableOperatorStats) getEntry(enRootOperatorStats);
	}

	@Override
	public int getNumberOfSubPlans() {
		return (Integer) getEntry(enNumberOfSubPlans);
	}

	@Override
	public ExecutablePlanStats getSubPlanStats( final int i ) throws NoSuchElementException {
		final List<?> subPlanStats = (List<?>) getEntry(enSubPlanStats);
		if ( i >= 0 && i < subPlanStats.size() ) {
			return (ExecutablePlanStats) subPlanStats.get(i);
		}

		throw new NoSuchElementException( "i: " + i + "; size of subPlanStats: " + subPlanStats.size() );
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<ExecutablePlanStats> getSubPlanStats() {
		return (List<ExecutablePlanStats>) getEntry(enSubPlanStats);
	}
}
