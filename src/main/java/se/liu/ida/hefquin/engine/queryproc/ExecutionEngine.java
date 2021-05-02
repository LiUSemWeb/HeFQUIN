package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;

public interface ExecutionEngine
{
	void execute( final ExecutablePlan plan, final QueryResultSink resultSink );
}
