package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Top-level base class for all implementations of {@link NullaryExecutableOp}.
 *
 * This base class handles the collection of statistics about both the
 * processing times of the operator. To this end, it implements the major
 * method of the {@link NullaryExecutableOp} interface, where the actual
 * functionality to be implemented for this method needs to be provided by
 * implementing the following abstract function in each sub-class of this
 * base class. This functions is:
 * <ul>
 * <li>{@link #_execute(IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class NullaryExecutableOpBase extends BaseForExecOps implements NullaryExecutableOp
{
	private int numberOfInvocations = 0;
	protected long timeAtExecStart  = 0L;
	protected long timeAtExecEnd    = 0L;

	public NullaryExecutableOpBase( final boolean collectExceptions,
	                                final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);
	}

	@Override
	public final void execute( final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		numberOfInvocations++;
		timeAtExecStart = System.currentTimeMillis();

		if ( collectExceptions ) {
			try {
				_execute(sink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				recordExceptionCaughtDuringExecution(e);
			}
		}
		else {
			_execute(sink, execCxt);
		}

		timeAtExecEnd = System.currentTimeMillis();
	}

	/**
	 * Implementations of this function need to execute the algorithm of this
	 * operator and send the result elements (if any) to the given sink.
	 *
	 * If an exception occurs during this process, then this exception needs
	 * to either be collected or be thrown, depending on whether {@link
	 * BaseForExecOps#collectExceptions} is set to <code>true</code>.
	 */
	protected abstract void _execute( final IntermediateResultElementSink sink,
                                      final ExecutionContext execCxt ) throws ExecOpExecutionException;

	@Override
	public void resetStats() {
		numberOfInvocations = 0;
		timeAtExecStart     = 0L;
		timeAtExecEnd       = 0L;
	}

	@Override
	public final ExecutableOperatorStats getStats() {
		return createStats();
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = new ExecutableOperatorStatsImpl(this);
		s.put( "numberOfInvocations",  Integer.valueOf(numberOfInvocations) );
		s.put( "overallExecTime",      Long.valueOf(timeAtExecEnd-timeAtExecStart) );
		s.put( "queryPlanningInfo",    qpInfo );
		return s;
	}

}
