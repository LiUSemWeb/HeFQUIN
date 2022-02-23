package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Top-level base class for all implementations of {@link NullaryExecutableOp}.
 */
public abstract class NullaryExecutableOpBase implements NullaryExecutableOp
{
	private int numberOfInvocations = 0;
	protected long timeAtExecStart  = 0L;
	protected long timeAtExecEnd    = 0L;

	@Override
	public final void execute( final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		numberOfInvocations++;
		timeAtExecStart = System.currentTimeMillis();

		_execute(sink, execCxt);

		timeAtExecEnd = System.currentTimeMillis();
	}

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
		return s;
	}

}