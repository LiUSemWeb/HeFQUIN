package se.liu.ida.hefquin.engine.queryplan;

import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.ResultElementIterator;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

import java.util.NoSuchElementException;

public interface ExecutablePlan
{
	void run( QueryResultSink resultSink );

    /**
     * Returns the root operator of this plan.
     */
    ExecutableOperator getRootOperator();

    ResultElementIterator getIterator();
}
