package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;

public interface ExecutionEngine
{
	ExecutionStats execute( final ExecutablePlan plan, final QueryResultSink resultSink )
			throws ExecutionException;
}
