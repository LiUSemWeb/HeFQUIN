package se.liu.ida.hefquin.engine.queryproc.impl;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.utils.StatsImpl;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;

public class QueryProcessingStatsAndExceptionsImpl extends StatsImpl implements QueryProcessingStatsAndExceptions
{
	protected static final String enOverallProcTime     = "overallQueryProcessingTime";
	protected static final String enPlanningTime        = "planningTime";
	protected static final String enCompilationTime     = "compilationTime";
	protected static final String enExecutionTime       = "executionTime";
	protected static final String enQueryPlanningStats  = "queryPlanningStats";
	protected static final String enExecStats           = "executionStats";

	protected final List<Exception> exceptions;

	public QueryProcessingStatsAndExceptionsImpl( final long overallQueryProcessingTime,
	                                              final long planningTime,
	                                              final long compilationTime,
	                                              final long executionTime,
	                                              final QueryPlanningStats queryPlanningStats,
	                                              final ExecutionStats execStats,
	                                              final List<Exception> exceptions )
	{
		put( enOverallProcTime, Long.valueOf(overallQueryProcessingTime) );
		put( enPlanningTime,    Long.valueOf(planningTime) );
		put( enCompilationTime, Long.valueOf(compilationTime) );
		put( enExecutionTime,   Long.valueOf(executionTime) );

		put( enQueryPlanningStats, queryPlanningStats );
		put( enExecStats,          execStats );

		if ( exceptions != null && exceptions.isEmpty() )
			this.exceptions = null;
		else
			this.exceptions = exceptions;
	}

	public QueryProcessingStatsAndExceptionsImpl( final long overallQueryProcessingTime,
	                                              final long planningTime,
	                                              final long compilationTime,
	                                              final long executionTime,
	                                              final QueryPlanningStats queryPlanningStats,
	                                              final ExecutionStats execStats )
	{
		this( overallQueryProcessingTime,
		      planningTime,
		      compilationTime,
		      executionTime,
		      queryPlanningStats,
		      execStats,
		      null );  // no exceptions
	}

	public QueryProcessingStatsAndExceptionsImpl( final QueryProcessingStatsAndExceptions other,
	                                              final Exception additionalException )
	{
		this( other.getOverallQueryProcessingTime(),
		      other.getPlanningTime(),
		      other.getCompilationTime(),
		      other.getExecutionTime(),
		      other.getQueryPlanningStats(),
		      other.getExecutionStats(),
		      copyAndAdd(other.getExceptions(), additionalException) );
	}

	protected static List<Exception> copyAndAdd( final List<Exception> otherExceptions,
	                                             final Exception additionalException ) {
		assert additionalException != null;

		if ( otherExceptions == null || otherExceptions.isEmpty() )
			return List.of(additionalException);

		final List<Exception> newList = new ArrayList<>( otherExceptions.size()+1 );
		newList.addAll(otherExceptions);
		newList.add(additionalException);
		return newList;
	}

	@Override
	public long getOverallQueryProcessingTime() {
		return (Long) getEntry(enOverallProcTime);
	}

	@Override
	public long getPlanningTime() {
		return (Long) getEntry(enPlanningTime);
	}

	@Override
	public long getCompilationTime() {
		return (Long) getEntry(enCompilationTime);
	}

	@Override
	public long getExecutionTime() {
		return (Long) getEntry(enExecutionTime);
	}

	@Override
	public QueryPlanningStats getQueryPlanningStats() {
		return (QueryPlanningStats) getEntry(enQueryPlanningStats);
	}

	@Override
	public ExecutionStats getExecutionStats() {
		return (ExecutionStats) getEntry(enExecStats);
	}

	@Override
	public boolean containsExceptions() {
		return exceptions != null;
	}

	@Override
	public List<Exception> getExceptions() {
		return exceptions;
	}

}
