package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlockBuilder;

public class ResultBlockIterOverResultElementIter implements ResultBlockIterator
{
	protected final ResultElementIterator eIter;
	protected final IntermediateResultBlockBuilder blockBuilder;
	protected final int blockSize;

	protected IntermediateResultBlock currentBlock = null;

	public ResultBlockIterOverResultElementIter( final ResultElementIterator eIter,
	                                             final IntermediateResultBlockBuilder blockBuilder,
	                                             final int blockSize ) {
		assert eIter != null;
		assert blockBuilder != null;

		this.eIter = eIter;
		this.blockBuilder = blockBuilder;
		this.blockSize = blockSize;
	}

	@Override
	public boolean hasNext() {
		if ( currentBlock == null ) {
			if ( ! eIter.hasNext() ) {
				return false;
			}

			blockBuilder.startNewBlock();
			while ( blockBuilder.sizeOfCurrentBlock() < blockSize && eIter.hasNext() ) {
				blockBuilder.add( eIter.next() );
			}
			currentBlock = blockBuilder.finishCurrentBlock();
		}

		return true;
	}

	@Override
	public IntermediateResultBlock next() {
		if ( ! hasNext() ) {
			throw new NoSuchElementException();
		}
		final IntermediateResultBlock returnBlock = currentBlock;
		currentBlock = null;
		return returnBlock;
	}

	@Override
	public ResultElementIterator getElementIterator() {
		return eIter;
	}
}
