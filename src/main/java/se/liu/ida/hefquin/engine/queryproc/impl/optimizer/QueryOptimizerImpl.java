package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.utils.Pair;

public class QueryOptimizerImpl implements QueryOptimizer
{
	protected final QueryOptimizationContext ctxt;

	public QueryOptimizerImpl( final QueryOptimizationContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	public Pair<PhysicalPlan, QueryOptimizationStats> optimize( final LogicalPlan initialPlan )
			throws QueryOptimizationException
	{
		final boolean keepMultiwayJoins = false;
		final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert(initialPlan, keepMultiwayJoins);

		final QueryOptimizationStats myStats = new QueryOptimizationStatsImpl();

		return new Pair<>(initialPhysicalPlan, myStats);

		// TODO implement query optimization
	}

}
