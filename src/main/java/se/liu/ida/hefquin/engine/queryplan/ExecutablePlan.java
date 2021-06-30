package se.liu.ida.hefquin.engine.queryplan;

import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

public interface ExecutablePlan
{
	void run( QueryResultSink resultSink ) throws ExecutionException;
}
