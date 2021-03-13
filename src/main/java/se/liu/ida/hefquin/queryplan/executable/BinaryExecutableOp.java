package se.liu.ida.hefquin.queryplan.executable;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public interface BinaryExecutableOp<InElmtType1,InElmtType2,OutElmtType>
                                      extends ExecutableOperator<OutElmtType>
{
	void processBlockFromChild1(
			final IntermediateResultBlock<InElmtType1> input,
            final IntermediateResultElementSink<OutElmtType> sink,
            final ExecutionContext execCxt );

	void processBlockFromChild2(
			final IntermediateResultBlock<InElmtType2> input,
            final IntermediateResultElementSink<OutElmtType> sink,
            final ExecutionContext execCxt );
}
