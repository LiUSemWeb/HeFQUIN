package se.liu.ida.hefquin.queryplan.executable;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public interface NullaryExecutableOp<OutElmtType> extends ExecutableOperator<OutElmtType>
{
	void execute( final IntermediateResultElementSink<OutElmtType> sink, final ExecutionContext execCxt );
}
