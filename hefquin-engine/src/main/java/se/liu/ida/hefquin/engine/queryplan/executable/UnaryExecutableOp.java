package se.liu.ida.hefquin.engine.queryplan.executable;

import java.util.List;

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
	 * Processes the solution mappings of the given list as input to this
	 * operator and sends the produced result elements (if any) to the given
	 * sink.
	 *
	 * The default implementation of this method simply calls
	 * {@link #process(SolutionMapping, IntermediateResultElementSink, ExecutionContext)}
	 * for every solution mapping obtained from the given list.
	 *
	 * Subclasses may override this behavior to send a greater number of output
	 * solution mappings to the given sink at a time (which is useful to reduce
	 * the communication between threads in the push-based execution model).
	 */
	default void process( final List<SolutionMapping> inputSolMaps,
	                      final IntermediateResultElementSink sink,
	                      final ExecutionContext execCxt ) throws ExecOpExecutionException {
		for ( final SolutionMapping sm : inputSolMaps ) {
			process(sm, sink, execCxt );
		}
	}

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
