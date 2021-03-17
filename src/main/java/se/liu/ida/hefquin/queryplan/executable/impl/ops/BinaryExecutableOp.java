package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementProducer;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public interface BinaryExecutableOp extends ExecutableOperator,
                                            IntermediateResultElementProducer
{
	/**
	 * Processes the given input coming from the first operand
	 * and sends the produced result elements (if any) to the
	 * given sink.
	 */
	void processBlockFromChild1(
			final IntermediateResultBlock input,
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt );

	/**
	 * Processes the given input coming from the second operand
	 * and sends the produced result elements (if any) to the
	 * given sink.
	 */
	void processBlockFromChild2(
			final IntermediateResultBlock input,
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
