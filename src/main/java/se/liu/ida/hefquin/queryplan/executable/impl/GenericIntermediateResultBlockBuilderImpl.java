package se.liu.ida.hefquin.queryplan.executable.impl;

import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlockBuilder;

public class GenericIntermediateResultBlockBuilderImpl<ElmtType>
                        implements IntermediateResultBlockBuilder<ElmtType>
{
	protected GenericIntermediateResultBlockImpl<ElmtType> block = null;

	@Override
	public void startNewBlock() {
		block = new GenericIntermediateResultBlockImpl<ElmtType>();
	}

	@Override
	public void add( ElmtType element ) {
		if ( block == null ) {
			startNewBlock();
		}

		block.add(element);
	}

	@Override
	public int sizeOfCurrentBlock() {
		return (block == null) ? 0 : block.size(); 
	}

	@Override
	public IntermediateResultBlock<ElmtType> finishCurrentBlock() {
		final IntermediateResultBlock<ElmtType> returnBlock = block;
		block = null;
		return returnBlock;
	}

}
