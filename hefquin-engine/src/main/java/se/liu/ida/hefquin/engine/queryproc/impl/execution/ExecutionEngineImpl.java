package se.liu.ida.hefquin.engine.queryproc.impl.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

public class ExecutionEngineImpl implements ExecutionEngine
{
	private static final Logger log = LoggerFactory.getLogger( ExecutionEngineImpl.class );

	@Override
	public ExecutionStats execute( final ExecutablePlan plan, final QueryResultSink resultSink )
			throws ExecutionException
	{
		log.debug("Starting executable plan execution.");

		plan.run(resultSink);

		log.debug("Executable plan execution finished.");

		return new ExecutionStatsImpl( plan.getStats() );
	}

}
