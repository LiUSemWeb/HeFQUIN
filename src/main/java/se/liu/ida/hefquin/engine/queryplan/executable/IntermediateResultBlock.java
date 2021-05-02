package se.liu.ida.hefquin.engine.queryplan.executable;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public interface IntermediateResultBlock
{
	/**
	 * Returns the number of intermediate result elements in this block.
	 */
	int size();

	/**
	 * Returns an iterator over the intermediate result elements in this block.
	 */
	Iterator<SolutionMapping> iterator();
	
}
