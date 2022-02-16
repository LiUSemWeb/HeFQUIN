package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementProducer;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public interface BinaryExecutableOp extends ExecutableOperator,
                                            IntermediateResultElementProducer
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
	 * Processes the given input coming from the first operand
	 * and sends the produced result elements (if any) to the
	 * given sink.
	 */
	void processBlockFromChild1(
			final IntermediateResultBlock input,
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Finishes up any processing related to the input coming
	 * from the first operand and sends the produced result
	 * elements (if any) to the given sink.
	 */
	void wrapUpForChild1( final IntermediateResultElementSink sink,
	                      final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Processes the given input coming from the second operand
	 * and sends the produced result elements (if any) to the
	 * given sink.
	 *
	 * May throw {@link IllegalStateException} for operators for which
	 * {@link #requiresCompleteChild1InputFirst()} returns true and
	 * {@link #wrapUpForChild1(IntermediateResultElementSink, ExecutionContext)}
	 * has not been called yet.
	 */
	void processBlockFromChild2(
			final IntermediateResultBlock input,
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Finishes up any processing related to the input coming
	 * from the second operand and sends the produced result
	 * elements (if any) to the given sink.
	 *
	 * May throw {@link IllegalStateException} for operators for which
	 * {@link #requiresCompleteChild1InputFirst()} returns true and
	 * {@link #wrapUpForChild1(IntermediateResultElementSink, ExecutionContext)}
	 * has not been called yet.
	 */
	void wrapUpForChild2( final IntermediateResultElementSink sink,
	                      final ExecutionContext execCxt ) throws ExecOpExecutionException;

	@Override
	ExecutableOperatorStats getStats();
}
