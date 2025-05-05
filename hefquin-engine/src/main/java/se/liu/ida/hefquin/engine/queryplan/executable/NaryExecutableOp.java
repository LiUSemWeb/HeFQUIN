package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A specialization of the {@link ExecutableOperator} interface that
 * captures executable operators that consume an arbitrary number of
 * sequences of solution mappings (where each such sequence is batched
 * into several blocks of solution mappings).
 */
public interface NaryExecutableOp extends ExecutableOperator
{
	/**
	 * Processes the given solution mapping as input coming from the
	 * x-th operand and sends the produced result elements (if any)
	 * to the given sink.
	 */
	void processInputFromXthChild( int x,
	                               SolutionMapping inputSolMap,
	                               IntermediateResultElementSink sink,
	                               ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Finishes up any processing related to the input coming
	 * from the x-th operand and sends the remaining result
	 * elements (if any) to the given sink.
	 *
	 * This method will be called once for each of the operands,
	 * after the sub-plan that produces the input coming from
	 * the operand has finished producing its result.
	 */
	void wrapUpForXthChild( int x,
	                        IntermediateResultElementSink sink,
	                        ExecutionContext execCxt ) throws ExecOpExecutionException;
}
