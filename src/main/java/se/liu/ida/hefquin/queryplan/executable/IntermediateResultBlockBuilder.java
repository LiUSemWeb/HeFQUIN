package se.liu.ida.hefquin.queryplan.executable;

import se.liu.ida.hefquin.data.SolutionMapping;

public interface IntermediateResultBlockBuilder
{
	/**
	 * Starts creating a new {@link IntermediateResultBlock}.
	 * 
	 * If creating the previous block has not been completed by calling
	 * {@link #finishCurrentBlock()}, then everything that was added into
	 * that uncompleted block will be discarded.
	 */
	void startNewBlock();

	/**
	 * Adds the given result element to the currently-created block.
	 * 
	 * If no block was started so far or this is the first call of this
	 * method after {@link #finishCurrentBlock()} was called, then a
	 * new block will be started first (i.e., {@link #startNewBlock()}
	 * will be called internally). 
	 */
	void add( SolutionMapping element );

	/**
	 * Returns the current size of the currently-created block.
	 * 
	 * If no block was started so far or this is the first call of this
	 * method after {@link #finishCurrentBlock()} was called, then this
	 * method returns 0 (zero).
	 */
	int sizeOfCurrentBlock();

	/**
	 * Finishes the creation of the currently-created block and returns this block.
	 */
	IntermediateResultBlock finishCurrentBlock();
}
