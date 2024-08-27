package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A specialization of the {@link ExecutableOperator} interface that
 * captures executable operators that consume a single input sequence
 * of solution mappings (which are batched into several blocks).
 */
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
	 *
	 * This method will be called only once, after the sub-plan
	 * that produces the input for this operator has finished
	 * producing its result.
	 */
	void concludeExecution( IntermediateResultElementSink sink,
	                        ExecutionContext execCxt ) throws ExecOpExecutionException;
}
