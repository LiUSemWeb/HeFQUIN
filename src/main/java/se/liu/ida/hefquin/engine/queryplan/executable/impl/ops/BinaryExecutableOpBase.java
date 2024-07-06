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
 *
 * This base class handles the collection of statistics about both the two
 * inputs and the processing times per input block from the two inputs. To
 * this end, it implements the major methods of the {@link BinaryExecutableOp}
 * interface, where the actual functionality to be implemented for these methods
 * needs to be provided by implementing four abstract functions in each sub-class
 * of this base class. These four functions are:
 * <ul>
 * <li>{@link #_processBlockFromChild1(IntermediateResultBlock, IntermediateResultElementSink, ExecutionContext)},</li>
 * <li>{@link #_processBlockFromChild2(IntermediateResultBlock, IntermediateResultElementSink, ExecutionContext)},</li>
 * <li>{@link #_wrapUpForChild1(IntermediateResultElementSink, ExecutionContext)}, and</li>
 * <li>{@link #_wrapUpForChild2(IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
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

	public BinaryExecutableOpBase( final boolean collectExceptions ) {
		super(collectExceptions);
	}

	@Override
	public final void processBlockFromChild1(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		timeAtCurrentLeftProcStart = System.currentTimeMillis();

		if ( collectExceptions ) {
			try {
				_processBlockFromChild1(input, sink, execCxt);
			}
			catch ( ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_processBlockFromChild1(input, sink, execCxt);
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
			final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		timeAtCurrentRightProcStart = System.currentTimeMillis();

		if ( collectExceptions ) {
			try {
				_processBlockFromChild2(input, sink, execCxt);
			}
			catch ( ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_processBlockFromChild2(input, sink, execCxt);
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


	/**
	 * Implementations of this function need to process the given input block
	 * coming from the first operand and send the produced result elements
	 * (if any) to the given sink.
	 *
	 * If an exception occurs while processing the input block, this exception
	 * needs to either be collected or be thrown, depending on whether {@link
	 * BaseForExecOps#collectExceptions} is set to <code>true</code>.
	 */
	protected abstract void _processBlockFromChild1(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Implementations of this function need to finish up any processing
	 * related to the input coming from the first operand and send the
	 * remaining result elements (if any) to the given sink.
	 *
	 * If an exception occurs during this process, then this exception needs
	 * to either be collected or be thrown, depending on whether {@link
	 * BaseForExecOps#collectExceptions} is set to <code>true</code>.
	 */
	protected abstract void _wrapUpForChild1( final IntermediateResultElementSink sink,
	                                          final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Implementations of this function need to process the given input block
	 * coming from the second operand and send the produced result elements
	 * (if any) to the given sink.
	 *
	 * If an exception occurs while processing the input block, this exception
	 * needs to either be collected or be thrown, depending on whether {@link
	 * BaseForExecOps#collectExceptions} is set to <code>true</code>.
	 */
	protected abstract void _processBlockFromChild2(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Implementations of this function need to finish up any processing
	 * related to the input coming from the second operand and send the
	 * remaining result elements (if any) to the given sink.
	 *
	 * If an exception occurs during this process, then this exception needs
	 * to either be collected or be thrown, depending on whether {@link
	 * BaseForExecOps#collectExceptions} is set to <code>true</code>.
	 */
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
