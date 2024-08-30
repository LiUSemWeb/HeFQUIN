package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public interface IntermediateResultElementSink
{
	void send( final SolutionMapping element );
}
