package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import java.util.Iterator;

import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;

public interface ResultBlockIterator<ElmtType>
       extends Iterator<IntermediateResultBlock<ElmtType>>
{

}
