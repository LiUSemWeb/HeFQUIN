package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpMultiwayUnion extends NaryExecutableOpBase
{
	public ExecOpMultiwayUnion( final int numberOfChildren, final boolean collectExceptions ) {
		super(numberOfChildren, collectExceptions);
	}

	@Override
	public int preferredInputBlockSizeFromChilden() {
		// Since this algorithm processes the input solution mappings
		// sequentially (one at a time), an input block size of 1 may
		// reduce the response time of the overall execution process.
		return 1;
	}

	@Override
	protected void _processBlockFromXthChild( final int x,
	                                          final IntermediateResultBlock input,
	                                          final IntermediateResultElementSink sink,
	                                          final ExecutionContext execCxt) {
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			sink.send(sm);
		}
	}

	@Override
	protected void _wrapUpForXthChild( final int x,
	                                   final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		// nothing to be done here
	}

}
