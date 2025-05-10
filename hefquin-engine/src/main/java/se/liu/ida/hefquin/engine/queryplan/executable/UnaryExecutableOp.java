package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A specialization of the {@link ExecutableOperator} interface that
 * captures executable operators that consume a single input sequence
 * of solution mappings.
 */
public interface UnaryExecutableOp extends ExecutableOperator
{
	/**
	 * Processes the given solution mapping as input to this operator and
	 * sends the produced result elements (if any) to the given sink.
	 */
	void process( SolutionMapping inputSolMap,
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
