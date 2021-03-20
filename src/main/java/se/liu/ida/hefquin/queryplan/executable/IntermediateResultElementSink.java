package se.liu.ida.hefquin.queryplan.executable;

import se.liu.ida.hefquin.data.SolutionMapping;

public interface IntermediateResultElementSink
{
	void send( final SolutionMapping element );
}
