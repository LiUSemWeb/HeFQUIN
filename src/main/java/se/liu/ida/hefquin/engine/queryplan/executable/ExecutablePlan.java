package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;
import se.liu.ida.hefquin.engine.utils.StatsProvider;

public interface ExecutablePlan extends StatsProvider
{
	void run( QueryResultSink resultSink ) throws ExecutionException;

	@Override
	ExecutablePlanStats getStats();
}
