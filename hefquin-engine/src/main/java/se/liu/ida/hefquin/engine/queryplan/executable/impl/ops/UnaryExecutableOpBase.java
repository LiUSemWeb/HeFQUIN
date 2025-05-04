package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
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
 * to be provided by implementing two abstract functions in each sub-class of
 * this base class. These two functions are:
 * <ul>
 * <li>{@link #_process(IntermediateResultBlock, IntermediateResultElementSink, ExecutionContext)} and</li>
 * <li>{@link #_concludeExecution(IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class UnaryExecutableOpBase extends BaseForExecOps implements UnaryExecutableOp
{
	private boolean executionConcluded          = false;
	private long numberOfInputMappingsProcessed = 0L;
	private long sumOfProcessingTimes           = 0L;
	private long minProcessingTime              = Long.MAX_VALUE;
	private long maxProcessingTime              = 0L;
	protected long timeAtCurrentProcStart       = 0L;

	public UnaryExecutableOpBase( final boolean collectExceptions ) {
		super(collectExceptions);
	}

	@Override
	public final void process( final SolutionMapping inputSolMap,
	                           final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt ) throws ExecOpExecutionException {
		timeAtCurrentProcStart = System.currentTimeMillis();

		if ( collectExceptions ) {
			try {
				_process(inputSolMap, sink, execCxt);
			}
			catch ( ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_process(inputSolMap, sink, execCxt);
		}

		final long processingTime = System.currentTimeMillis() - timeAtCurrentProcStart;

		sumOfProcessingTimes += processingTime;
		if ( processingTime < minProcessingTime ) { minProcessingTime = processingTime; }
		if ( processingTime > maxProcessingTime ) { maxProcessingTime = processingTime; }

		numberOfInputMappingsProcessed++;
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
		sumOfProcessingTimes           = 0L;
		minProcessingTime              = Long.MAX_VALUE;
		maxProcessingTime              = 0L;
		timeAtCurrentProcStart         = 0L;
	}

	@Override
	public final ExecutableOperatorStats getStats() {
		return createStats();
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = new ExecutableOperatorStatsImpl(this);

		final double avgProcTime = (numberOfInputMappingsProcessed==0)
				? 0 : sumOfProcessingTimes/numberOfInputMappingsProcessed;

		s.put( "executionConcluded",                Boolean.valueOf(executionConcluded) );
		s.put( "numberOfInputMappingsProcessed",    Long.valueOf(numberOfInputMappingsProcessed) );
		s.put( "totalProcTimeForAllInputMappings",  Long.valueOf(sumOfProcessingTimes) );
		s.put( "averageProcTimePerInputMapping",    Double.valueOf(avgProcTime) );
		s.put( "minimumProcTimePerInputMapping",    Long.valueOf(minProcessingTime) );
		s.put( "maximumProcTimePerInputMapping",    Long.valueOf(maxProcessingTime) );
		return s;
	}

}
