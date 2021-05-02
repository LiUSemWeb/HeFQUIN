package se.liu.ida.hefquin.engine.queryproc.impl.execution;

import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

public class ExecutionEngineImpl implements ExecutionEngine
{
	@Override
	public void execute( final ExecutablePlan plan, final QueryResultSink resultSink ) {
		plan.run(resultSink);
	}

}
