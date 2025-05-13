package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Top-level base class for all implementations of {@link NaryExecutableOp}.
 *
 * This base class handles the collection of statistics about each of the
 * inputs and about the processing times per input block from each of the
 * inputs. To this end, this base class implements the major methods of the
 * {@link NaryExecutableOp} interface, where the actual functionality to be
 * implemented for these methods needs to be provided by implementing two
 * abstract functions in each sub-class of this base class.
 * These two functions are:
 * <ul>
 * <li>{@link #_processInputFromXthChild(int, SolutionMapping, IntermediateResultElementSink, ExecutionContext)} and</li>
 * <li>{@link #_wrapUpForXthChild(int, IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class NaryExecutableOpBase extends BaseForExecOps implements NaryExecutableOp
{
	protected final int numberOfChildren;

	private boolean[] xthInputConsumed;
	private long[] numberOfMappingsFromXthInputProcessed;
	private long[] sumOfProcessingTimesXthInput;
	private long[] minProcessingTimeXthInput;
	private long[] maxProcessingTimeXthInput;
	protected long[] timeAtCurrentProcStartXthInput;

	public NaryExecutableOpBase( final int numberOfChildren,
	                             final boolean collectExceptions ) {
		super(collectExceptions);

		this.numberOfChildren = numberOfChildren;

		xthInputConsumed                       = new boolean[numberOfChildren];
		numberOfMappingsFromXthInputProcessed  = new long[numberOfChildren];
		sumOfProcessingTimesXthInput           = new long[numberOfChildren];
		minProcessingTimeXthInput              = new long[numberOfChildren];
		maxProcessingTimeXthInput              = new long[numberOfChildren];
		timeAtCurrentProcStartXthInput         = new long[numberOfChildren];

		resetStats();
	}

	@Override
	public final void processInputFromXthChild( final int x,
	                                            final SolutionMapping inputSolMap,
	                                            final IntermediateResultElementSink sink,
	                                            final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		assert x >= 0;
		assert x < numberOfChildren;

		timeAtCurrentProcStartXthInput[x] = System.currentTimeMillis();

		if ( collectExceptions ) {
			try {
				_processInputFromXthChild(x, inputSolMap, sink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_processInputFromXthChild(x, inputSolMap, sink, execCxt);
		}

		final long processingTime = System.currentTimeMillis() - timeAtCurrentProcStartXthInput[x];

		sumOfProcessingTimesXthInput[x] += processingTime;
		if ( processingTime < minProcessingTimeXthInput[x] ) { minProcessingTimeXthInput[x] = processingTime; }
		if ( processingTime > maxProcessingTimeXthInput[x] ) { maxProcessingTimeXthInput[x] = processingTime; }

		numberOfMappingsFromXthInputProcessed[x]++;
	}

	@Override
	public final void processInputFromXthChild( final int x,
	                                            final List<SolutionMapping> inputSolMaps,
	                                            final IntermediateResultElementSink sink,
	                                            final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		assert x >= 0;
		assert x < numberOfChildren;

		if ( collectExceptions ) {
			try {
				_processInputFromXthChild(x, inputSolMaps, sink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_processInputFromXthChild(x, inputSolMaps, sink, execCxt);
		}

		numberOfMappingsFromXthInputProcessed[x] += inputSolMaps.size();
	}

	@Override
	public final void wrapUpForXthChild( final int x,
	                                     final IntermediateResultElementSink sink,
	                                     final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		assert x >= 0;
		assert x < numberOfChildren;

		xthInputConsumed[x] = true;
		_wrapUpForXthChild(x, sink, execCxt);
	}

	@Override
	public void resetStats() {
		for ( int i = 0; i < numberOfChildren; i++ ) {
			xthInputConsumed[i] = false;
			numberOfMappingsFromXthInputProcessed[i] = 0L;
			sumOfProcessingTimesXthInput[i] = 0L;
			minProcessingTimeXthInput[i] = Long.MAX_VALUE;
			maxProcessingTimeXthInput[i] = 0L;
			timeAtCurrentProcStartXthInput[i] = 0L;
		}
	}


	/**
	 * Implementations of this function need to process the given solution
	 * mapping as input coming from the x-th operand and send the produced
	 * result elements (if any) to the given sink.
	 *
	 * If an exception occurs while processing the solution mapping, then
	 * this exception needs to be thrown.
	 */
	protected abstract void _processInputFromXthChild(
			int x,
			SolutionMapping inputSolMap,
			IntermediateResultElementSink sink,
			ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Processes the input solution mappings of the given list by calling
	 * {@link #_processInputFromXthChild(int, SolutionMapping, IntermediateResultElementSink, ExecutionContext)}
	 * for each of them.
	 *
	 * Subclasses may override this behavior to send a greater number of output
	 * solution mappings to the given sink at a time (which is useful to reduce
	 * the communication between threads in the push-based execution model).
	 * If an exception occurs within the overriding implementation, then this
	 * exception needs to be thrown.
	 */
	protected void _processInputFromXthChild(
			final int x,
			final List<SolutionMapping> inputSolMaps,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException {
		for ( final SolutionMapping sm : inputSolMaps ) {
			_processInputFromXthChild(x, sm, sink, execCxt );
		}
	}

	/**
	 * Implementations of this function need to finish up any processing
	 * related to the input coming from the x-th operand and send the
	 * remaining result elements (if any) to the given sink.
	 *
	 * If an exception occurs while processing the solution mapping, then
	 * this exception needs to be thrown.
	 */
	protected abstract void _wrapUpForXthChild(
			int x,
			IntermediateResultElementSink sink,
			ExecutionContext execCxt ) throws ExecOpExecutionException;

	@Override
	public final ExecutableOperatorStats getStats() {
		return createStats();
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = new ExecutableOperatorStatsImpl(this);
		s.put( "xthInputConsumed", xthInputConsumed );
		s.put( "numberOfMappingsFromXthInputProcessed",  numberOfMappingsFromXthInputProcessed );
		return s;
	}

}
