package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.utils.Pair;

public class PhysicalQueryOptimizerImpl implements PhysicalOptimizer
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
	public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final LogicalPlan initialPlan )
			throws PhysicalOptimizationException
	{
		final boolean keepMultiwayJoins = false;
		final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert(initialPlan, keepMultiwayJoins);

		final PhysicalOptimizationStats myStats = new PhysicalQueryOptimizationStatsImpl();

		return new Pair<>(initialPhysicalPlan, myStats);

		// TODO implement query optimization
	}

}
