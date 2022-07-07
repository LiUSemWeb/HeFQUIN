package se.liu.ida.hefquin.engine.queryproc.impl.planning;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningException;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;
import se.liu.ida.hefquin.engine.utils.Pair;

/**
 * Simple implementation of {@link QueryPlanner}.
 */
public class QueryPlannerImpl implements QueryPlanner
{
	protected final SourcePlanner sourcePlanner;
	protected final QueryOptimizer optimizer;

	public QueryPlannerImpl( final SourcePlanner sourcePlanner,
	                         final QueryOptimizer optimizer ) {
		assert sourcePlanner != null;
		assert optimizer != null;

		this.sourcePlanner = sourcePlanner;
		this.optimizer = optimizer;
	}

	@Override
	public SourcePlanner getSourcePlanner() { return sourcePlanner; }

	@Override
	public QueryOptimizer getOptimizer() { return optimizer; }

	@Override
	public Pair<PhysicalPlan, QueryPlanningStats> createPlan( final Query query ) throws QueryPlanningException {
		final long t1 = System.currentTimeMillis();
		final Pair<LogicalPlan, SourcePlanningStats> saAndStats = sourcePlanner.createSourceAssignment(query);

		final long t2 = System.currentTimeMillis();
		final Pair<PhysicalPlan, QueryOptimizationStats> planAndStats = optimizer.optimize( saAndStats.object1 );

		final long t3 = System.currentTimeMillis();

		final QueryPlanningStats myStats = new QueryPlanningStatsImpl( t3-t1, t2-t1, t3-t2, saAndStats.object2, planAndStats.object2 );

		return new Pair<>(planAndStats.object1, myStats);
	}

}
