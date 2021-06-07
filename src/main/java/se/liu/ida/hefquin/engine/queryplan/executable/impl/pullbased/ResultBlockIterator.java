package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;

public interface ResultBlockIterator extends Iterator<IntermediateResultBlock>
{

    ResultElementIterator getElementIterator();
}
