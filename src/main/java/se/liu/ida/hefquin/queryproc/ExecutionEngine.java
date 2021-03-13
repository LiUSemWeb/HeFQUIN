package se.liu.ida.hefquin.queryproc;

import se.liu.ida.hefquin.queryplan.ExecutablePlan;

public interface ExecutionEngine
{
	void execute( final ExecutablePlan plan, final QueryResultSink resultSink );
}
