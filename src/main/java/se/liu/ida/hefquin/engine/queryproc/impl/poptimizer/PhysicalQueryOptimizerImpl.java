package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizer;
import se.liu.ida.hefquin.engine.utils.Pair;

public class PhysicalQueryOptimizerImpl implements PhysicalQueryOptimizer
{
	protected final QueryOptimizationContext ctxt;

	public PhysicalQueryOptimizerImpl( final QueryOptimizationContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	public boolean assumesLogicalMultiwayJoins() {
		return false;
	}

	@Override
	public Pair<PhysicalPlan, PhysicalQueryOptimizationStats> optimize( final LogicalPlan initialPlan )
			throws PhysicalQueryOptimizationException
	{
		final boolean keepMultiwayJoins = false;
		final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert(initialPlan, keepMultiwayJoins);

		final PhysicalQueryOptimizationStats myStats = new PhysicalQueryOptimizationStatsImpl();

		return new Pair<>(initialPhysicalPlan, myStats);

		// TODO implement query optimization
	}

}
