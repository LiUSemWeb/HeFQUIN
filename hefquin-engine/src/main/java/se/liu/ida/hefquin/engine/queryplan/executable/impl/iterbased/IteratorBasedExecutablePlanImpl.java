package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

public class IteratorBasedExecutablePlanImpl implements ExecutablePlan
{
	private static final Logger log = LoggerFactory.getLogger( IteratorBasedExecutablePlanImpl.class );

	protected final ResultElementIterator it;

	public IteratorBasedExecutablePlanImpl( final ResultElementIterator it ) {
		assert it != null;
		this.it = it;
	}

	@Override
	public void run( final QueryResultSink resultSink ) throws ExecutionException {
		log.debug( "Starting iterator-based executable plan execution." );
		try {
			while ( it.hasNext() ) {
				resultSink.send( it.next() );
			}
		}
		catch ( final ResultElementIterException ex ) {
			throw new ExecutionException( "An exception occurred during result iteration.", ex.getWrappedExecutionException() );
		}
		log.debug( "Iterator-based executable plan execution finished successfully." );
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
