package se.liu.ida.hefquin.engine.queryplan.executable;

import java.util.List;

import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;
import se.liu.ida.hefquin.engine.utils.StatsProvider;

public interface ExecutablePlan extends StatsProvider
{
	void run( QueryResultSink resultSink ) throws ExecutionException;

	@Override
	ExecutablePlanStats getStats();

	/**
	 * Returns exceptions that were caught and collected during the
	 * execution of this plan (if any). If no exceptions were caught
	 * (which should be the normal case), then this function returns
	 * an empty list.
	 */
	List<Exception> getExceptionsCaughtDuringExecution();
}
