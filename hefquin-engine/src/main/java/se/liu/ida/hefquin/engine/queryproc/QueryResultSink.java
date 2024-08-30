package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public interface QueryResultSink
{
	void send( SolutionMapping element );
}
