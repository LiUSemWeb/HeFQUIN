package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

public class IteratorBasedExecutablePlanImpl implements ExecutablePlan
{
	protected final ResultElementIterator it;

	public IteratorBasedExecutablePlanImpl( final ResultElementIterator it ) {
		assert it != null;
		this.it = it;
	}

	@Override
	public void run( final QueryResultSink resultSink ) throws ExecutionException {
		try {
			while ( it.hasNext() ) {
				resultSink.send( it.next() );
			}
		}
		catch ( final ResultElementIterException ex ) {
			throw new ExecutionException( "An exception occurred during result iteration.", ex.getWrappedExecutionException() );
		}
	}

	@Override
	public void resetStats() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ExecutablePlanStats getStats() {
		return ResultIteratorUtils.tryGetStatsOfProducingSubPlan(it);
	}

	@Override
	public List<Exception> getExceptionsCaughtDuringExecution() {
		return ResultIteratorUtils.tryGetExceptionsOfProducingSubPlan(it);
	}

}
