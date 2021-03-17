package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementProducer;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public interface NullaryExecutableOp extends ExecutableOperator,
                                             IntermediateResultElementProducer
{
	/**
	 * Executes this operator and sends the produced
	 * result elements (if any) to the given sink.
	 */
	void execute( final IntermediateResultElementSink sink,
	              final ExecutionContext execCxt );
}
