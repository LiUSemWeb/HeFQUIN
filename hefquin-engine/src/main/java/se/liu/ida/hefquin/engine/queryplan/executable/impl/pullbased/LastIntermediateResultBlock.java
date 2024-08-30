package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;

public class LastIntermediateResultBlock implements IntermediateResultBlock
{
	protected final IntermediateResultBlock wrappedBlock;

	public LastIntermediateResultBlock( final IntermediateResultBlock wrappedBlock ) {
		assert wrappedBlock != null;
		this.wrappedBlock = wrappedBlock;
	}

	public IntermediateResultBlock getWrappedBlock() {
		return wrappedBlock;
	}

	@Override
	public int size() {
		return wrappedBlock.size();
	}

	@Override
	public Iterable<SolutionMapping> getSolutionMappings() {
		return wrappedBlock.getSolutionMappings();
	}
}
