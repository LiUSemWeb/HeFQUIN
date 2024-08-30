package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlockBuilder;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;

/**
 * An implementation of {@link IntermediateResultElementSink} that
 * materializes all solution mappings that are sent to it directly
 * into an {@link IntermediateResultBlock}. This block can then be
 * obtained by calling {@link #getMaterializedResultBlock()}.
 *
 * If more solution mappings are sent to this sink after the block
 * has been requested, a new block will be started, which can then
 * be obtained by calling {@link #getMaterializedResultBlock()} again.
 *
 * Attention, this implementation is not thread safe.
 */
public class MaterializingIntermediateResultElementSink implements IntermediateResultElementSink
{
	protected final IntermediateResultBlockBuilder blockBuilder = new GenericIntermediateResultBlockBuilderImpl();

	@Override
	public void send( final SolutionMapping element ) {
		blockBuilder.add(element);
	}

	/**
	 * Returns the number of solution mappings that have currently been
	 * accumulated to be placed in the {@link IntermediateResultBlock} to
	 * be returned by {@link #getMaterializedResultBlock()}.
	 */
	public int getSizeOfCurrentResultBlock() {
		return blockBuilder.sizeOfCurrentBlock();
	}

	/**
	 * Returns an {@link IntermediateResultBlock} that contains all
	 * solution mappings that have been sent to this sink since the
	 * last time {@link #getMaterializedResultBlock()} was called or,
	 * if this function has not been called so far, since this sink
	 * was created.
	 */
	public IntermediateResultBlock getMaterializedResultBlock() {
		return blockBuilder.finishCurrentBlock();
	}
}
