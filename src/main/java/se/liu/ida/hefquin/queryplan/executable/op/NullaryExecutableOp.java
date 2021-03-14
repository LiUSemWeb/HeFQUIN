package se.liu.ida.hefquin.queryplan.executable.op;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public interface NullaryExecutableOp<OutElmtType> extends ExecutableOperator<OutElmtType>
{
	/**
	 * Executes this operator and sends the produced
	 * result elements (if any) to the given sink.
	 */
	void execute( final IntermediateResultElementSink<OutElmtType> sink,
	              final ExecutionContext execCxt );
}
