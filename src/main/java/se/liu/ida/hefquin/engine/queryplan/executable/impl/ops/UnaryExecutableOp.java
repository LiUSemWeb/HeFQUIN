package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementProducer;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public interface UnaryExecutableOp extends ExecutableOperator,
                                           IntermediateResultElementProducer
{
	/**
	 * Processes the given input and sends the produced
	 * result elements (if any) to the given sink.
	 */
	void process( final IntermediateResultBlock input,
	              final IntermediateResultElementSink sink,
	              final ExecutionContext execCxt ) throws ExecOpExecutionException;
	/**
	 * Concludes the execution of this operator and sends
	 * the produced result elements (if any) to the given
	 * sink.
	 */
	void concludeExecution( final IntermediateResultElementSink sink,
	                        final ExecutionContext execCxt ) throws ExecOpExecutionException;
}
