package se.liu.ida.hefquin.queryplan.executable;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public interface UnaryExecutableOp<InElmtType,OutElmtType>
                             extends ExecutableOperator<OutElmtType>
{
	void process( final IntermediateResultBlock<InElmtType> input,
	              final IntermediateResultElementSink<OutElmtType> sink,
	              final ExecutionContext execCxt );

}
