package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlockBuilder;

public class ResultBlockIterOverResultElementIter<ElmtType> implements ResultBlockIterator<ElmtType>
{
	protected final ResultElementIterator<ElmtType> eIter;
	protected final IntermediateResultBlockBuilder<ElmtType> blockBuilder;
	protected final int blockSize;

	protected IntermediateResultBlock<ElmtType> currentBlock = null;

	public ResultBlockIterOverResultElementIter( final ResultElementIterator<ElmtType> eIter,
	                                             final IntermediateResultBlockBuilder<ElmtType> blockBuilder,
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
	public IntermediateResultBlock<ElmtType> next() {
		if ( ! hasNext() ) {
			throw new NoSuchElementException();
		}
		final IntermediateResultBlock<ElmtType> returnBlock = currentBlock;
		currentBlock = null;
		return returnBlock;
	}

}
