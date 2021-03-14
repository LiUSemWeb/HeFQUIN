package se.liu.ida.hefquin.queryplan.executable.op;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementProducer;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public interface UnaryExecutableOp<InElmtType,OutElmtType>
                             extends ExecutableOperator<OutElmtType>,
                                     IntermediateResultElementProducer<OutElmtType>
{
	/**
	 * Processes the given input and sends the produced
	 * result elements (if any) to the given sink.
	 */
	void process( final IntermediateResultBlock<InElmtType> input,
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
