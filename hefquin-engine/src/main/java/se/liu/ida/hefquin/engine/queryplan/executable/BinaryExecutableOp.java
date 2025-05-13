package se.liu.ida.hefquin.engine.queryplan.executable;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A specialization of the {@link ExecutableOperator} interface that
 * captures executable operators that consume a pair of two sequences
 * of solution mappings (which both are batched into several blocks of
 * solution mappings).
 */
public interface BinaryExecutableOp extends ExecutableOperator
{
	/**
	 * Returns true if this operator is implemented based on
	 * the assumption that the COMPLETE input from the first
	 * operand has been sent to it before input from the
	 * second operand is sent.
	 *
	 * An example of an operator that may return true here is
	 * a hash join (which first needs to add all result elements
	 * from the first operand into its hash table and, then, can
	 * start consuming the result elements from the second operand
	 * by probing into the hash table). In contrast, a symmetric
	 * hash join (which has two hash tables and can consume result
	 * elements from both inputs in any order) would return false.
	 *
	 * Operators that return true here may throw an {@link IllegalStateException} if their methods
	 * {@link #processBlockFromChild2(IntermediateResultBlock, IntermediateResultElementSink, ExecutionContext)}
	 * or {@link #wrapUpForChild2(IntermediateResultElementSink, ExecutionContext)} are called before
	 * {@link #wrapUpForChild1(IntermediateResultElementSink, ExecutionContext)} has been called.
	 */
	boolean requiresCompleteChild1InputFirst();

	/**
	 * Processes the given solution mapping as input coming from the
	 * first operand and sends the produced result elements (if any)
	 * to the given sink.
	 */
	void processInputFromChild1( SolutionMapping inputSolMap,
	                             IntermediateResultElementSink sink,
	                             ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Processes the solution mappings of the given list as input coming
	 * from the first operand and sends the produced result elements (if
	 * any) to the given sink.
	 *
	 * The default implementation of this method simply calls
	 * {@link #processInputFromChild1(SolutionMapping, IntermediateResultElementSink, ExecutionContext)}
	 * for every solution mapping of the given list
	 *
	 * Subclasses may override this behavior to send a greater number of output
	 * solution mappings to the given sink at a time (which is useful to reduce
	 * the communication between threads in the push-based execution model).
	 */
	default void processInputFromChild1( final List<SolutionMapping> inputSolMaps,
	                                     final IntermediateResultElementSink sink,
	                                     final ExecutionContext execCxt ) throws ExecOpExecutionException {
		for ( final SolutionMapping sm : inputSolMaps ) {
			processInputFromChild1(sm, sink, execCxt );
		}
	}

	/**
	 * Finishes up any processing related to the input coming
	 * from the first operand and sends the remaining result
	 * elements (if any) to the given sink.
	 *
	 * This method will be called only once, after the sub-plan
	 * that produces the input coming from the first operand has
	 * finished producing its result.
	 */
	void wrapUpForChild1( IntermediateResultElementSink sink,
	                      ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Processes the given solution mapping as input coming from the
	 * second operand and sends the produced result elements (if any)
	 * to the given sink.
	 *
	 * May throw {@link IllegalStateException} for operators for which
	 * {@link #requiresCompleteChild1InputFirst()} returns true and
	 * {@link #wrapUpForChild1(IntermediateResultElementSink, ExecutionContext)}
	 * has not been called yet.
	 */
	void processInputFromChild2( SolutionMapping inputSolMap,
	                             IntermediateResultElementSink sink,
	                             ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Processes the solution mappings of the given list as input coming
	 * from the second operand and sends the produced result elements (if
	 * any) to the given sink.
	 *
	 * The default implementation of this method simply calls
	 * {@link #processInputFromChild2(SolutionMapping, IntermediateResultElementSink, ExecutionContext)}
	 * for every solution mapping obtained from the given list.
	 *
	 * Subclasses may override this behavior to send a greater number of output
	 * solution mappings to the given sink at a time (which is useful to reduce
	 * the communication between threads in the push-based execution model).
	 */
	default void processInputFromChild2( final List<SolutionMapping> inputSolMaps,
	                                     final IntermediateResultElementSink sink,
	                                     final ExecutionContext execCxt ) throws ExecOpExecutionException {
		for ( final SolutionMapping sm : inputSolMaps ) {
			processInputFromChild2(sm, sink, execCxt );
		}
	}

	/**
	 * Finishes up any processing related to the input coming
	 * from the second operand and sends the remaining result
	 * elements (if any) to the given sink.
	 *
	 * This method will be called only once, after the sub-plan
	 * that produces the input coming from the second operand
	 * has finished producing its result.
	 *
	 * May throw {@link IllegalStateException} for operators for which
	 * {@link #requiresCompleteChild1InputFirst()} returns true and
	 * {@link #wrapUpForChild1(IntermediateResultElementSink, ExecutionContext)}
	 * has not been called yet.
	 */
	void wrapUpForChild2( IntermediateResultElementSink sink,
	                      ExecutionContext execCxt ) throws ExecOpExecutionException;
}
