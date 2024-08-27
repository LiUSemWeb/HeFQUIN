package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

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
 * and the processing times per input block. To this end, it implements the
 * major methods of the {@link UnaryExecutableOp} interface, where the actual
 * functionality to be implemented for these methods needs to be provided by
 * implementing two abstract functions in each sub-class of this base class.
 * These two functions are:
 * <ul>
 * <li>{@link #_process(IntermediateResultBlock, IntermediateResultElementSink, ExecutionContext)} and</li>
 * <li>{@link #_concludeExecution(IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class UnaryExecutableOpBase extends BaseForExecOps implements UnaryExecutableOp
{
	private boolean executionConcluded          = false;
	private int numberOfInputBlocksProcessed    = 0;
	private long numberOfInputMappingsProcessed = 0L;
	private long sumOfProcessingTimes           = 0L;
	private long minProcessingTime              = Long.MAX_VALUE;
	private long maxProcessingTime              = 0L;
	protected long timeAtCurrentProcStart       = 0L;

	public UnaryExecutableOpBase( final boolean collectExceptions ) {
		super(collectExceptions);
	}

	@Override
	public final void process( final IntermediateResultBlock input,
	                           final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt ) throws ExecOpExecutionException {
		timeAtCurrentProcStart = System.currentTimeMillis();

		if ( collectExceptions ) {
			try {
				_process(input, sink, execCxt);
			}
			catch ( ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_process(input, sink, execCxt);
		}

		final long processingTime = System.currentTimeMillis() - timeAtCurrentProcStart;

		sumOfProcessingTimes += processingTime;
		if ( processingTime < minProcessingTime ) { minProcessingTime = processingTime; }
		if ( processingTime > maxProcessingTime ) { maxProcessingTime = processingTime; }

		numberOfInputMappingsProcessed += input.size();
		numberOfInputBlocksProcessed++;
	}

	@Override
	public final void concludeExecution( final IntermediateResultElementSink sink,
	                                     final ExecutionContext execCxt ) throws ExecOpExecutionException {
		_concludeExecution(sink, execCxt);

		executionConcluded = true;
	}

	/**
	 * Implementations of this function need to process the given input block
	 * and send the produced result elements (if any) to the given sink.
	 *
	 * If an exception occurs while processing the input block, this exception
	 * needs to either be collected or be thrown, depending on whether {@link
	 * BaseForExecOps#collectExceptions} is set to <code>true</code>.
	 */
	protected abstract void _process( final IntermediateResultBlock input,
	                                  final IntermediateResultElementSink sink,
	                                  final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Implementations of this function need to conclude the execution of
	 * this operator and send the remaining result elements (if any) to the
	 * given sink.
	 *
	 * If an exception occurs during this process, then this exception needs
	 * to either be collected or be thrown, depending on whether {@link
	 * BaseForExecOps#collectExceptions} is set to <code>true</code>.
	 */
	protected abstract void _concludeExecution( final IntermediateResultElementSink sink,
	                                            final ExecutionContext execCxt ) throws ExecOpExecutionException;


	@Override
	public void resetStats() {
		executionConcluded             = false;
		numberOfInputBlocksProcessed   = 0;
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

		final double avgProcTime = (numberOfInputBlocksProcessed==0)
				? 0 : sumOfProcessingTimes/numberOfInputBlocksProcessed;

		s.put( "executionConcluded",              Boolean.valueOf(executionConcluded) );
		s.put( "numberOfInputBlocksProcessed",    Integer.valueOf(numberOfInputBlocksProcessed) );
		s.put( "numberOfInputMappingsProcessed",  Long.valueOf(numberOfInputMappingsProcessed) );
		s.put( "averageProcTimePerInputBlock",    Double.valueOf(avgProcTime) );
		s.put( "minimumProcTimePerInputBlock",    Long.valueOf(minProcessingTime) );
		s.put( "maximumProcTimePerInputBlock",    Long.valueOf(maxProcessingTime) );
		return s;
	}

}
