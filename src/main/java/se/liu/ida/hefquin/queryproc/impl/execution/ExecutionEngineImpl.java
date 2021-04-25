package se.liu.ida.hefquin.queryproc.impl.execution;

import se.liu.ida.hefquin.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.queryproc.QueryResultSink;

public class ExecutionEngineImpl implements ExecutionEngine
{
	@Override
	public void execute( final ExecutablePlan plan, final QueryResultSink resultSink ) {
		plan.run(resultSink);
	}

}
