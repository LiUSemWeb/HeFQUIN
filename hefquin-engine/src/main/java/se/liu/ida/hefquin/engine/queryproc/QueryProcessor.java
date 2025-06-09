package se.liu.ida.hefquin.engine.queryproc;

import java.util.List;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.utils.Pair;

public interface QueryProcessor
{
	Pair<QueryProcStats, List<Exception>> processQuery( final Query query, final QueryResultSink resultSink )
			throws QueryProcException;

	QueryPlanner getPlanner();
	QueryPlanCompiler getPlanCompiler();
	ExecutionEngine getExecutionEngine();

	/**
	 * Shuts down all thread pools associated with this query processor.
	 */
	void shutdown();
}
