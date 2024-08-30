package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlockBuilder;

public class GenericIntermediateResultBlockBuilderImpl
                        implements IntermediateResultBlockBuilder
{
	protected GenericIntermediateResultBlockImpl block = null;

	@Override
	public void startNewBlock() {
		block = new GenericIntermediateResultBlockImpl();
	}

	@Override
	public void add( SolutionMapping element ) {
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
	public IntermediateResultBlock finishCurrentBlock() {
		final IntermediateResultBlock returnBlock = block;
		block = null;
		return returnBlock;
	}

}
