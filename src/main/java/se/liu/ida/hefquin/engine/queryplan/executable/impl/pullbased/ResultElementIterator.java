package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;

public interface ResultElementIterator extends Iterator<SolutionMapping>
{
    ExecutableOperator getOp();

    int getArity();

    ResultElementIterator getSubIterator(final int i) throws NoSuchElementException;
}
