package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementProducer;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public interface NullaryExecutableOp extends ExecutableOperator,
                                             IntermediateResultElementProducer
{
	/**
	 * Executes this operator and sends the produced
	 * result elements (if any) to the given sink.
	 */
	void execute( final IntermediateResultElementSink sink,
	              final ExecutionContext execCxt ) throws ExecOpExecutionException;

	@Override
	ExecutableOperatorStats getStats();
}
