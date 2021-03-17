package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementProducer;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public interface UnaryExecutableOp extends ExecutableOperator,
                                           IntermediateResultElementProducer
{
	/**
	 * Processes the given input and sends the produced
	 * result elements (if any) to the given sink.
	 */
	void process( final IntermediateResultBlock input,
	              final IntermediateResultElementSink sink,
	              final ExecutionContext execCxt );
	/**
	 * Concludes the execution of this operator and sends
	 * the produced result elements (if any) to the given
	 * sink.
	 */
	void concludeExecution( final IntermediateResultElementSink sink,
	                        final ExecutionContext execCxt );
}
