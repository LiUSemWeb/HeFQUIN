package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

public class PullBasedPlan implements ExecutablePlan
{
	protected final ResultElementIterator it;

	public PullBasedPlan( final ResultElementIterator it ) {
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

}
