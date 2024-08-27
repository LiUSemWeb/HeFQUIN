package se.liu.ida.hefquin.engine.queryproc.impl.execution;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

public class ExecutionEngineImpl implements ExecutionEngine
{
	@Override
	public ExecutionStats execute( final ExecutablePlan plan, final QueryResultSink resultSink )
			throws ExecutionException
	{
		plan.run(resultSink);

		return new ExecutionStatsImpl( plan.getStats() );
	}

}
