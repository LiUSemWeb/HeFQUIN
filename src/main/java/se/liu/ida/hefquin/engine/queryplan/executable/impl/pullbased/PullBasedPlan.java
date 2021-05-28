package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

import java.util.NoSuchElementException;

public class PullBasedPlan implements ExecutablePlan
{
	protected final ResultElementIterator it;

	public PullBasedPlan( final ResultElementIterator it ) {
		assert it != null;
		this.it = it;
	}

	@Override
	public void run( final QueryResultSink resultSink ) {
		while ( it.hasNext() ) {
			resultSink.send( it.next() );
		}
	}

	@Override
	public ExecutableOperator getRootOperator() {
		return it.getOp();
	}

	@Override
	public ResultElementIterator getIterator() {
		return it;
	}

}
