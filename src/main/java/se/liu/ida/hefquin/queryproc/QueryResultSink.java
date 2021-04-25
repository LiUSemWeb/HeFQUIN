package se.liu.ida.hefquin.queryproc;

import se.liu.ida.hefquin.data.SolutionMapping;

public interface QueryResultSink
{
	void send( SolutionMapping element );
}
