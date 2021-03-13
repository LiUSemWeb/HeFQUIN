package se.liu.ida.hefquin.queryproc;

import se.liu.ida.hefquin.query.Query;

public interface QueryProcessor
{
	void processQuery( final Query query, final QueryResultSink resultSink );

	QueryPlanner getPlanner();
	QueryPlanCompiler getPlanCompiler();
	ExecutionEngine getExecutionEngine();
}
