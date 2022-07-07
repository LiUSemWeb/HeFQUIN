package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public interface NullaryExecutableOp extends ExecutableOperator,
                                             IntermediateResultElementProducer
{
	/**
	 * Executes this operator and sends the produced
	 * result elements (if any) to the given sink.
	 */
	void execute( IntermediateResultElementSink sink,
	              ExecutionContext execCxt ) throws ExecOpExecutionException;
}
