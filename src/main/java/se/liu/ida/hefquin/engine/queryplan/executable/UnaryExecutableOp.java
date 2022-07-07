package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public interface UnaryExecutableOp extends ExecutableOperator
{
	/**
	 * Returns the preferred block size of input blocks
	 * that are passed to this executable operator.
	 *
	 * A query planner may use this number as an optimization
	 * hint but it does not have to use it.
	 */
	int preferredInputBlockSize();

	/**
	 * Processes the given input and sends the produced
	 * result elements (if any) to the given sink.
	 */
	void process( IntermediateResultBlock input,
	              IntermediateResultElementSink sink,
	              ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Concludes the execution of this operator and sends the
	 * remaining result elements (if any) to the given sink.
	 */
	void concludeExecution( IntermediateResultElementSink sink,
	                        ExecutionContext execCxt ) throws ExecOpExecutionException;
}
