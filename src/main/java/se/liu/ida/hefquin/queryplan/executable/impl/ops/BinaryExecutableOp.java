package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementProducer;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public interface BinaryExecutableOp<InElmtType1,InElmtType2,OutElmtType>
                                      extends ExecutableOperator<OutElmtType>,
                                              IntermediateResultElementProducer<OutElmtType>
{
	/**
	 * Processes the given input coming from the first operand
	 * and sends the produced result elements (if any) to the
	 * given sink.
	 */
	void processBlockFromChild1(
			final IntermediateResultBlock<InElmtType1> input,
            final IntermediateResultElementSink<OutElmtType> sink,
            final ExecutionContext execCxt );

	/**
	 * Processes the given input coming from the second operand
	 * and sends the produced result elements (if any) to the
	 * given sink.
	 */
	void processBlockFromChild2(
			final IntermediateResultBlock<InElmtType2> input,
            final IntermediateResultElementSink<OutElmtType> sink,
            final ExecutionContext execCxt );
	/**
	 * Concludes the execution of this operator and sends
	 * the produced result elements (if any) to the given
	 * sink.
	 */
	void concludeExecution( final IntermediateResultElementSink<OutElmtType> sink,
	                        final ExecutionContext execCxt );
}
