package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.query.Query;

public interface QueryProcessor
{
	void processQuery( final Query query, final QueryResultSink resultSink )
			throws QueryProcException;

	QueryPlanner getPlanner();
	QueryPlanCompiler getPlanCompiler();
	ExecutionEngine getExecutionEngine();
}
