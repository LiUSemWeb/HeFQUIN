package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizer;
import se.liu.ida.hefquin.engine.utils.Pair;

public class QueryOptimizerImpl implements PhysicalQueryOptimizer
{
	protected final QueryOptimizationContext ctxt;

	public QueryOptimizerImpl( final QueryOptimizationContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	public Pair<PhysicalPlan, PhysicalQueryOptimizationStats> optimize( final LogicalPlan initialPlan )
			throws PhysicalQueryOptimizationException
	{
		final boolean keepMultiwayJoins = false;
		final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert(initialPlan, keepMultiwayJoins);

		final PhysicalQueryOptimizationStats myStats = new QueryOptimizationStatsImpl();

		return new Pair<>(initialPhysicalPlan, myStats);

		// TODO implement query optimization
	}

}
