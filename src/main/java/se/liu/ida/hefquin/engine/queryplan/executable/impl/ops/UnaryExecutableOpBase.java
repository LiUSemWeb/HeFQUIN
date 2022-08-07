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

	protected abstract void _process( final IntermediateResultBlock input,
	                                  final IntermediateResultElementSink sink,
	                                  final ExecutionContext execCxt ) throws ExecOpExecutionException;

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
