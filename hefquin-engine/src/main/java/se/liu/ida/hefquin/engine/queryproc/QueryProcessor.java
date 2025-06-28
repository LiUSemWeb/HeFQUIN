package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.base.query.Query;

public interface QueryProcessor
{
	QueryProcessingStatsAndExceptions processQuery( Query query,
	                                                QueryResultSink resultSink )
			throws QueryProcException;

	QueryPlanner getPlanner();
	QueryPlanCompiler getPlanCompiler();
	ExecutionEngine getExecutionEngine();

	/**
	 * Shuts down all thread pools associated with this query processor.
	 */
	void shutdown();
}
