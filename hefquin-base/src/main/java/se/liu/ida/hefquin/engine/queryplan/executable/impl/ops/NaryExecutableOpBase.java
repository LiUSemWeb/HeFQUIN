package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
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
 * <li>{@link #_processBlockFromXthChild(int, IntermediateResultBlock, IntermediateResultElementSink, ExecutionContext)} and</li>
 * <li>{@link #_wrapUpForXthChild(int, IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class NaryExecutableOpBase extends BaseForExecOps implements NaryExecutableOp
{
	protected final int numberOfChildren;

	private boolean[] xthInputConsumed;
	private int[] numberOfBlocksFromXthInputProcessed;
	private long[] numberOfMappingsFromXthInputProcessed;
	private long[] sumOfProcessingTimesXthInput;
	private long[] minProcessingTimeXthInput;
	private long[] maxProcessingTimeXthInput;
	protected long[] timeAtCurrentProcStartXthInput;

	public NaryExecutableOpBase( final int numberOfChildren, final boolean collectExceptions ) {
		super(collectExceptions);

		this.numberOfChildren = numberOfChildren;

		xthInputConsumed                   = new boolean[numberOfChildren];
		numberOfBlocksFromXthInputProcessed   = new int[numberOfChildren];
		numberOfMappingsFromXthInputProcessed = new long[numberOfChildren];
		sumOfProcessingTimesXthInput           = new long[numberOfChildren];
		minProcessingTimeXthInput              = new long[numberOfChildren];
		maxProcessingTimeXthInput              = new long[numberOfChildren];
		timeAtCurrentProcStartXthInput         = new long[numberOfChildren];

		resetStats();
	}

	@Override
	public final void processBlockFromXthChild( final int x,
	                                            final IntermediateResultBlock input,
	                                            final IntermediateResultElementSink sink,
	                                            final ExecutionContext execCxt ) throws ExecOpExecutionException {
		assert x >= 0;
		assert x < numberOfChildren;

		timeAtCurrentProcStartXthInput[x] = System.currentTimeMillis();

		if ( collectExceptions ) {
			try {
				_processBlockFromXthChild(x, input, sink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_processBlockFromXthChild(x, input, sink, execCxt);
		}

		final long processingTime = System.currentTimeMillis() - timeAtCurrentProcStartXthInput[x];

		sumOfProcessingTimesXthInput[x] += processingTime;
		if ( processingTime < minProcessingTimeXthInput[x] ) { minProcessingTimeXthInput[x] = processingTime; }
		if ( processingTime > maxProcessingTimeXthInput[x] ) { maxProcessingTimeXthInput[x] = processingTime; }

		numberOfMappingsFromXthInputProcessed[x] += input.size();
		numberOfBlocksFromXthInputProcessed[x]++;
	}

	@Override
	public final void wrapUpForXthChild( int x,
            IntermediateResultElementSink sink,
            ExecutionContext execCxt ) throws ExecOpExecutionException {
		assert x >= 0;
		assert x < numberOfChildren;

		xthInputConsumed[x] = true;
		_wrapUpForXthChild(x, sink, execCxt);
	}

	@Override
	public void resetStats() {
		for ( int i = 0; i < numberOfChildren; i++ ) {
			xthInputConsumed[i] = false;
			numberOfBlocksFromXthInputProcessed[i] = 0;
			numberOfMappingsFromXthInputProcessed[i] = 0L;
			sumOfProcessingTimesXthInput[i] = 0L;
			minProcessingTimeXthInput[i] = Long.MAX_VALUE;
			maxProcessingTimeXthInput[i] = 0L;
			timeAtCurrentProcStartXthInput[i] = 0L;
		}
	}


	/**
	 * Implementations of this function need to process the given input block
	 * coming from the x-th operand and send the produced result elements (if
	 * any) to the given sink.
	 *
	 * If an exception occurs while processing the input block, this exception
	 * needs to either be collected or be thrown, depending on whether {@link
	 * BaseForExecOps#collectExceptions} is set to <code>true</code>.
	 */
	protected abstract void _processBlockFromXthChild(
			final int x,
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;

	/**
	 * Implementations of this function need to finish up any processing
	 * related to the input coming from the x-th operand and send the
	 * remaining result elements (if any) to the given sink.
	 *
	 * If an exception occurs during this process, then this exception needs
	 * to either be collected or be thrown, depending on whether {@link
	 * BaseForExecOps#collectExceptions} is set to <code>true</code>.
	 */
	protected abstract void _wrapUpForXthChild( final int x,
	                                            final IntermediateResultElementSink sink,
	                                            final ExecutionContext execCxt ) throws ExecOpExecutionException;

	@Override
	public final ExecutableOperatorStats getStats() {
		return createStats();
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = new ExecutableOperatorStatsImpl(this);
		s.put( "xthInputConsumed", xthInputConsumed );
		s.put( "numberOfBlocksFromXthInputProcessed",    numberOfBlocksFromXthInputProcessed );
		s.put( "numberOfMappingsFromXthInputProcessed",  numberOfMappingsFromXthInputProcessed );
		return s;
	}
}
