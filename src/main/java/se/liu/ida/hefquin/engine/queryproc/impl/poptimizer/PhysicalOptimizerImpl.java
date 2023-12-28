package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.utils.Pair;

public class PhysicalOptimizerImpl extends PhysicalOptimizerBase
{
	public PhysicalOptimizerImpl( final LogicalToPhysicalPlanConverter l2pConverter ) {
		super(l2pConverter);
	}

	@Override
	public boolean assumesLogicalMultiwayJoins() {
		return false;
	}

	@Override
	public boolean keepMultiwayJoinsInInitialPhysicalPlan() {
		return false;
	}

	@Override
	public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final PhysicalPlan initialPlan )
			throws PhysicalOptimizationException
	{
		final PhysicalOptimizationStats myStats = new PhysicalOptimizationStatsImpl();

		return new Pair<>(initialPlan, myStats);

		// TODO implement query optimization
	}

}
