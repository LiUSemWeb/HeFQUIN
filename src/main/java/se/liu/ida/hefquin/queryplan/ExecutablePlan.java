package se.liu.ida.hefquin.queryplan;

import se.liu.ida.hefquin.queryproc.QueryResultSink;

public interface ExecutablePlan
{
	void run( QueryResultSink resultSink );
}
