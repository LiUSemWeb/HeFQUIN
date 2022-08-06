package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Top-level base class for all implementations of {@link BinaryExecutableOp}.
 */
public abstract class BinaryExecutableOpBase extends BaseForExecOps implements BinaryExecutableOp
{
	private boolean leftInputConsumed           = false;
	private boolean rightInputConsumed          = false;

	private int numberOfLeftInputBlocksProcessed    = 0;
	private long numberOfLeftInputMappingsProcessed = 0L;
	private long sumOfLeftProcessingTimes           = 0L;
	private long minLeftProcessingTime              = Long.MAX_VALUE;
	private long maxLeftProcessingTime              = 0L;
	protected long timeAtCurrentLeftProcStart       = 0L;

	private int numberOfRightInputBlocksProcessed    = 0;
	private long numberOfRightInputMappingsProcessed = 0L;
	private long sumOfRightProcessingTimes           = 0L;
	private long minRightProcessingTime              = Long.MAX_VALUE;
	private long maxRightProcessingTime              = 0L;
	protected long timeAtCurrentRightProcStart       = 0L;

	@Override
	public final void processBlockFromChild1(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt )
	{
		timeAtCurrentLeftProcStart = System.currentTimeMillis();

		try {
			_processBlockFromChild1(input, sink, execCxt);
		}
		catch ( ExecOpExecutionException e ) {
			recordExceptionCaughtDuringExecution(e);
		}

		final long processingTime = System.currentTimeMillis() - timeAtCurrentLeftProcStart;

		sumOfLeftProcessingTimes += processingTime;
		if ( processingTime < minLeftProcessingTime ) { minLeftProcessingTime = processingTime; }
		if ( processingTime > maxLeftProcessingTime ) { maxLeftProcessingTime = processingTime; }

		numberOfLeftInputMappingsProcessed += input.size();
		numberOfLeftInputBlocksProcessed++;
	}

	@Override
	public final void wrapUpForChild1( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) throws ExecOpExecutionException {
		leftInputConsumed = true;
		_wrapUpForChild1(sink, execCxt);
	}

	@Override
	public final void processBlockFromChild2(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt )
	{
		timeAtCurrentRightProcStart = System.currentTimeMillis();

		try {
			_processBlockFromChild2(input, sink, execCxt);
		}
		catch ( ExecOpExecutionException e ) {
			recordExceptionCaughtDuringExecution(e);
		}

		final long processingTime = System.currentTimeMillis() - timeAtCurrentRightProcStart;

		sumOfRightProcessingTimes += processingTime;
		if ( processingTime < minRightProcessingTime ) { minRightProcessingTime = processingTime; }
		if ( processingTime > maxRightProcessingTime ) { maxRightProcessingTime = processingTime; }

		numberOfRightInputMappingsProcessed += input.size();
		numberOfRightInputBlocksProcessed++;
	}

	@Override
	public final void wrapUpForChild2( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) throws ExecOpExecutionException {
		rightInputConsumed = true;
		_wrapUpForChild2(sink, execCxt);
	}


	protected abstract void _processBlockFromChild1(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;

	protected abstract void _wrapUpForChild1( final IntermediateResultElementSink sink,
	                                          final ExecutionContext execCxt ) throws ExecOpExecutionException;

	protected abstract void _processBlockFromChild2(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;

	protected abstract void _wrapUpForChild2( final IntermediateResultElementSink sink,
	                                          final ExecutionContext execCxt ) throws ExecOpExecutionException;


	@Override
	public void resetStats() {
		leftInputConsumed  = false;
		rightInputConsumed = false;

		numberOfLeftInputBlocksProcessed   = 0;
		numberOfLeftInputMappingsProcessed = 0L;
		sumOfLeftProcessingTimes           = 0L;
		minLeftProcessingTime              = Long.MAX_VALUE;
		maxLeftProcessingTime              = 0L;
		timeAtCurrentLeftProcStart         = 0L;

		numberOfRightInputBlocksProcessed   = 0;
		numberOfRightInputMappingsProcessed = 0L;
		sumOfRightProcessingTimes           = 0L;
		minRightProcessingTime              = Long.MAX_VALUE;
		maxRightProcessingTime              = 0L;
		timeAtCurrentRightProcStart         = 0L;
	}

	@Override
	public final ExecutableOperatorStats getStats() {
		return createStats();
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = new ExecutableOperatorStatsImpl(this);
		s.put( "leftInputConsumed",              Boolean.valueOf(leftInputConsumed) );
		s.put( "rightInputConsumed",             Boolean.valueOf(rightInputConsumed) );

		final double avgProcTimeLeft = (numberOfLeftInputBlocksProcessed==0)
				? 0 : sumOfLeftProcessingTimes/numberOfLeftInputBlocksProcessed;

		s.put( "numberOfLeftInputBlocksProcessed",    Integer.valueOf(numberOfLeftInputBlocksProcessed) );
		s.put( "numberOfLeftInputMappingsProcessed",  Long.valueOf(numberOfLeftInputMappingsProcessed) );
		s.put( "averageProcTimePerLeftInputBlock",    Double.valueOf(avgProcTimeLeft) );
		s.put( "minimumProcTimePerLeftInputBlock",    Long.valueOf(minLeftProcessingTime) );
		s.put( "maximumProcTimePerLeftInputBlock",    Long.valueOf(maxLeftProcessingTime) );

		final double avgProcTimeRight = (numberOfRightInputBlocksProcessed==0)
				? 0 : sumOfRightProcessingTimes/numberOfRightInputBlocksProcessed;

		s.put( "numberOfRightInputBlocksProcessed",    Integer.valueOf(numberOfRightInputBlocksProcessed) );
		s.put( "numberOfRightInputMappingsProcessed",  Long.valueOf(numberOfRightInputMappingsProcessed) );
		s.put( "averageProcTimePerRightInputBlock",    Double.valueOf(avgProcTimeRight) );
		s.put( "minimumProcTimePerRightInputBlock",    Long.valueOf(minRightProcessingTime) );
		s.put( "maximumProcTimePerRightInputBlock",    Long.valueOf(maxRightProcessingTime) );
		return s;
	}
}
