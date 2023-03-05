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

	protected abstract void _processBlockFromXthChild(
			final int x,
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException;

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
