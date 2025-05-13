package se.liu.ida.hefquin.engine.queryplan.executable;

import java.util.List;

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
	 * Processes the solution mappings oft he given list as input coming from
	 * the x-th operand and sends the produced result elements (if any) to the
	 * given sink.
	 *
	 * The default implementation of this method simply calls
	 * {@link #processInputFromXthChild(int, SolutionMapping, IntermediateResultElementSink, ExecutionContext)}
	 * for every solution mapping of the given list
	 *
	 * Subclasses may override this behavior to send a greater number of output
	 * solution mappings to the given sink at a time (which is useful to reduce
	 * the communication between threads in the push-based execution model).
	 */
	default void processInputFromXthChild( final int x,
	                                       final List<SolutionMapping> inputSolMaps,
	                                       final IntermediateResultElementSink sink,
	                                       final ExecutionContext execCxt ) throws ExecOpExecutionException {
		for ( final SolutionMapping sm : inputSolMaps ) {
			processInputFromXthChild(x, sm, sink, execCxt );
		}
	}

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
