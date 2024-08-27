package se.liu.ida.hefquin.engine.queryproc;

import java.util.List;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.utils.Pair;

public interface QueryProcessor
{
	Pair<QueryProcStats, List<Exception>> processQuery( final Query query, final QueryResultSink resultSink )
			throws QueryProcException;

	QueryPlanner getPlanner();
	QueryPlanCompiler getPlanCompiler();
	ExecutionEngine getExecutionEngine();
}
