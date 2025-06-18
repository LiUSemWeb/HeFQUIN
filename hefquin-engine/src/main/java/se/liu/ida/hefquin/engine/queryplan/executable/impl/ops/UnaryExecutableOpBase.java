package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Top-level base class for all implementations of {@link UnaryExecutableOp}.
 *
 * This base class handles the collection of statistics about both the input
 * and the processing times per input solution mapping. To this end, it
 * implements the major methods of the {@link UnaryExecutableOp} interface,
 * where the actual functionality to be implemented for these methods needs
 * to be provided by implementing two abstract functions in each subclass of
 * this base class. These two functions are:
 * <ul>
 * <li>{@link #_process(SolutionMapping, IntermediateResultElementSink, ExecutionContext)} and</li>
 * <li>{@link #_concludeExecution(IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 * Additionally, subclasses may override the following function:
 * <ul>
 * <li>{@link #_process(List, IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class UnaryExecutableOpBase extends BaseForExecOps implements UnaryExecutableOp
{
	private boolean executionConcluded = false;
	private long numberOfInputMappingsProcessed = 0L;

	public UnaryExecutableOpBase( final boolean collectExceptions ) {
		super(collectExceptions);
	}

	@Override
	public final void process( final SolutionMapping inputSolMap,
	                           final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		if ( collectExceptions ) {
			try {
				_process(inputSolMap, sink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_process(inputSolMap, sink, execCxt);
		}

		numberOfInputMappingsProcessed++;
	}

	@Override
	public final void process( final List<SolutionMapping> inputSolMaps,
	                           final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		if ( collectExceptions ) {
			try {
				_process(inputSolMaps, sink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_process(inputSolMaps, sink, execCxt);
		}

		numberOfInputMappingsProcessed += inputSolMaps.size();
	}

	@Override
	public final void concludeExecution( final IntermediateResultElementSink sink,
	                                     final ExecutionContext execCxt ) throws ExecOpExecutionException {
		if ( collectExceptions ) {
			try {
				_concludeExecution(sink, execCxt);
			}
			catch ( ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_concludeExecution(sink, execCxt);
		}

		executionConcluded = true;
	}

	/**
	 * Implementations of this function need to process the given solution
	 * mapping as input and send the produced result elements (if any) to
	 * the given sink.
	 *
	 * If an exception occurs while processing the solution mapping, then
	 * this exception needs to be thrown.
	 */
	protected abstract void _process( final SolutionMapping inputSolMap,
	                                  final IntermediateResultElementSink sink,
	                                  final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Processes the input solution mappings of the given list by calling
	 * {@link #_process(SolutionMapping, IntermediateResultElementSink, ExecutionContext)}
	 * for each of them.
	 *
	 * Subclasses may override this behavior to send a greater number of output
	 * solution mappings to the given sink at a time (which is useful to reduce
	 * the communication between threads in the push-based execution model).
	 * If an exception occurs within the overriding implementation, then this
	 * exception needs to be thrown.
	 */
	protected void _process( final List<SolutionMapping> inputSolMaps,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			throws ExecOpExecutionException {
		for ( final SolutionMapping sm : inputSolMaps ) {
			_process(sm, sink, execCxt );
		}
	}

	/**
	 * Implementations of this function need to conclude the execution of this
	 * operator and send the remaining result elements (if any) to the given
	 * sink.
	 *
	 * If an exception occurs during this process, then this exception needs
	 * to be thrown.
	 */
	protected abstract void _concludeExecution( final IntermediateResultElementSink sink,
	                                            final ExecutionContext execCxt ) throws ExecOpExecutionException;


	@Override
	public void resetStats() {
		executionConcluded             = false;
		numberOfInputMappingsProcessed = 0L;
	}

	@Override
	public final ExecutableOperatorStats getStats() {
		return createStats();
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = new ExecutableOperatorStatsImpl(this);
		s.put( "executionConcluded",                Boolean.valueOf(executionConcluded) );
		s.put( "numberOfInputMappingsProcessed",    Long.valueOf(numberOfInputMappingsProcessed) );
		return s;
	}

}
