package se.liu.ida.hefquin.queryplan.executable;

import java.util.Iterator;

public interface IntermediateResultBlock<ElmtType>
{
	/**
	 * Returns the number of intermediate result elements in this block.
	 */
	int size();

	/**
	 * Returns an iterator over the intermediate result elements in this block.
	 */
	Iterator<ElmtType> iterator();
	
}
