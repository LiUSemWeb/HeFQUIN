package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
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
 * <li>{@link #_processInputFromChild1(SolutionMapping, IntermediateResultElementSink, ExecutionContext)},</li>
 * <li>{@link #_processInputFromChild2(SolutionMapping, IntermediateResultElementSink, ExecutionContext)},</li>
 * <li>{@link #_wrapUpForChild1(IntermediateResultElementSink, ExecutionContext)}, and</li>
 * <li>{@link #_wrapUpForChild2(IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class BinaryExecutableOpBase extends BaseForExecOps implements BinaryExecutableOp
{
	private boolean leftInputConsumed           = false;
	private boolean rightInputConsumed          = false;

	private long numberOfLeftInputMappingsProcessed = 0L;
	private long sumOfLeftProcessingTimes           = 0L;
	private long minLeftProcessingTime              = Long.MAX_VALUE;
	private long maxLeftProcessingTime              = 0L;
	protected long timeAtCurrentLeftProcStart       = 0L;

	private long numberOfRightInputMappingsProcessed = 0L;
	private long sumOfRightProcessingTimes           = 0L;
	private long minRightProcessingTime              = Long.MAX_VALUE;
	private long maxRightProcessingTime              = 0L;
	protected long timeAtCurrentRightProcStart       = 0L;

	public BinaryExecutableOpBase( final boolean collectExceptions ) {
		super(collectExceptions);
	}

	@Override
	public final void processInputFromChild1(
			final SolutionMapping inputSolMap,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		timeAtCurrentLeftProcStart = System.currentTimeMillis();

		if ( collectExceptions ) {
			try {
				_processInputFromChild1(inputSolMap, sink, execCxt);
			}
			catch ( ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_processInputFromChild1(inputSolMap, sink, execCxt);
		}

		final long processingTime = System.currentTimeMillis() - timeAtCurrentLeftProcStart;

		sumOfLeftProcessingTimes += processingTime;
		if ( processingTime < minLeftProcessingTime ) { minLeftProcessingTime = processingTime; }
		if ( processingTime > maxLeftProcessingTime ) { maxLeftProcessingTime = processingTime; }

		numberOfLeftInputMappingsProcessed++;
	}

	@Override
	public final void processInputFromChild1(
			final List<SolutionMapping> inputSolMaps,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		if ( collectExceptions ) {
			try {
				_processInputFromChild1(inputSolMaps, sink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_processInputFromChild1(inputSolMaps, sink, execCxt);
		}

		numberOfLeftInputMappingsProcessed += inputSolMaps.size();
	}

	@Override
	public final void wrapUpForChild1( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) throws ExecOpExecutionException {
		leftInputConsumed = true;
		_wrapUpForChild1(sink, execCxt);
	}

	@Override
	public final void processInputFromChild2(
			final SolutionMapping inputSolMap,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		timeAtCurrentRightProcStart = System.currentTimeMillis();

		if ( collectExceptions ) {
			try {
				_processInputFromChild2(inputSolMap, sink, execCxt);
			}
			catch ( ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_processInputFromChild2(inputSolMap, sink, execCxt);
		}

		final long processingTime = System.currentTimeMillis() - timeAtCurrentRightProcStart;

		sumOfRightProcessingTimes += processingTime;
		if ( processingTime < minRightProcessingTime ) { minRightProcessingTime = processingTime; }
		if ( processingTime > maxRightProcessingTime ) { maxRightProcessingTime = processingTime; }

		numberOfRightInputMappingsProcessed++;
	}

	@Override
	public final void processInputFromChild2(
			final List<SolutionMapping> inputSolMaps,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		if ( collectExceptions ) {
			try {
				_processInputFromChild2(inputSolMaps, sink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_processInputFromChild2(inputSolMaps, sink, execCxt);
		}

		numberOfRightInputMappingsProcessed += inputSolMaps.size();
	}

	@Override
	public final void wrapUpForChild2( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) throws ExecOpExecutionException {
		rightInputConsumed = true;
		_wrapUpForChild2(sink, execCxt);
	}


	/**
	 * Implementations of this function need to process the given solution
	 * mapping as input coming from the first operand and send the produced
	 * result elements (if any) to the given sink.
	 *
	 * If an exception occurs while processing the solution mapping, then
	 * this exception needs to be thrown.
	 */
	protected abstract void _processInputFromChild1(
			final SolutionMapping inputSolMap,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Processes the input solution mappings of the given list by calling
	 * {@link #_processInputFromChild1(SolutionMapping, IntermediateResultElementSink, ExecutionContext)}
	 * for each of them.
	 *
	 * Subclasses may override this behavior to send a greater number of output
	 * solution mappings to the given sink at a time (which is useful to reduce
	 * the communication between threads in the push-based execution model).
	 * If an exception occurs within the overriding implementation, then this
	 * exception needs to be thrown.
	 */
	protected void _processInputFromChild1(
			final List<SolutionMapping> inputSolMaps,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException {
		for ( final SolutionMapping sm : inputSolMaps ) {
			_processInputFromChild1(sm, sink, execCxt );
		}
	}

	/**
	 * Implementations of this function need to finish up any processing
	 * related to the input coming from the first operand and send the
	 * remaining result elements (if any) to the given sink.
	 *
	 * If an exception occurs during this process, then this exception
	 * needs to be thrown.
	 */
	protected abstract void _wrapUpForChild1(
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Implementations of this function need to process the given solution
	 * mapping as input coming from the second operand and send the produced
	 * result elements (if any) to the given sink.
	 *
	 * If an exception occurs while processing the solution mapping, then
	 * this exception needs to be thrown.
	 *
	 * May throw an {@link IllegalStateException} for operators for which
	 * {@link #requiresCompleteChild1InputFirst()} returns true and
	 * {@link #_wrapUpForChild1(IntermediateResultElementSink, ExecutionContext)}
	 * has not been called yet.
	 */
	protected abstract void _processInputFromChild2(
			final SolutionMapping inputSolMap,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Processes the input solution mappings of the given list by calling
	 * {@link #_processInputFromChild2(SolutionMapping, IntermediateResultElementSink, ExecutionContext)}
	 * for each of them.
	 *
	 * Subclasses may override this behavior to send a greater number of output
	 * solution mappings to the given sink at a time (which is useful to reduce
	 * the communication between threads in the push-based execution model).
	 * If an exception occurs within the overriding implementation, then this
	 * exception needs to be thrown.
	 */
	protected void _processInputFromChild2(
			final List<SolutionMapping> inputSolMaps,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException {
		for ( final SolutionMapping sm : inputSolMaps ) {
			_processInputFromChild2(sm, sink, execCxt );
		}
	}

	/**
	 * Implementations of this function need to finish up any processing
	 * related to the input coming from the second operand and send the
	 * remaining result elements (if any) to the given sink.
	 *
	 * If an exception occurs during this process, then this exception
	 * needs to be thrown.
	 *
	 * May throw an {@link IllegalStateException} for operators for which
	 * {@link #requiresCompleteChild1InputFirst()} returns true and
	 * {@link #_wrapUpForChild1(IntermediateResultElementSink, ExecutionContext)}
	 * has not been called yet.
	 */
	protected abstract void _wrapUpForChild2(
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;


	@Override
	public void resetStats() {
		leftInputConsumed  = false;
		rightInputConsumed = false;

		numberOfLeftInputMappingsProcessed = 0L;
		sumOfLeftProcessingTimes           = 0L;
		minLeftProcessingTime              = Long.MAX_VALUE;
		maxLeftProcessingTime              = 0L;
		timeAtCurrentLeftProcStart         = 0L;

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

		final double avgProcTimeLeft = (numberOfLeftInputMappingsProcessed==0L)
				? 0 : sumOfLeftProcessingTimes/numberOfLeftInputMappingsProcessed;

		s.put( "numberOfLeftInputMappingsProcessed",  Long.valueOf(numberOfLeftInputMappingsProcessed) );
		s.put( "averageProcTimePerLeftInputBlock",    Double.valueOf(avgProcTimeLeft) );
		s.put( "minimumProcTimePerLeftInputBlock",    Long.valueOf(minLeftProcessingTime) );
		s.put( "maximumProcTimePerLeftInputBlock",    Long.valueOf(maxLeftProcessingTime) );

		final double avgProcTimeRight = (numberOfRightInputMappingsProcessed==0L)
				? 0 : sumOfRightProcessingTimes/numberOfRightInputMappingsProcessed;

		s.put( "numberOfRightInputMappingsProcessed",  Long.valueOf(numberOfRightInputMappingsProcessed) );
		s.put( "averageProcTimePerRightInputBlock",    Double.valueOf(avgProcTimeRight) );
		s.put( "minimumProcTimePerRightInputBlock",    Long.valueOf(minRightProcessingTime) );
		s.put( "maximumProcTimePerRightInputBlock",    Long.valueOf(maxRightProcessingTime) );
		return s;
	}
}
