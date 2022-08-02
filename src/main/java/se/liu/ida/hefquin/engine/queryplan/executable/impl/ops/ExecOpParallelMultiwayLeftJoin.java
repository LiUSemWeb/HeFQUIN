package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpParallelMultiwayLeftJoin extends UnaryExecutableOpBase
{
	protected final List<LogicalOpRequest<?,?>> optionalParts;

	public ExecOpParallelMultiwayLeftJoin( final List<LogicalOpRequest<?,?>> optionalParts,
	                                       final ExpectedVariables inputVarsFromNonOptionalPart ) {
		assert ! optionalParts.isEmpty();
		this.optionalParts = optionalParts;
	}

	@Override
	public int preferredInputBlockSize() {
		return 30;
	}

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) throws ExecOpExecutionException {
		// TODO ...
		
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) throws ExecOpExecutionException {
		// nothing to be done here
	}

}
