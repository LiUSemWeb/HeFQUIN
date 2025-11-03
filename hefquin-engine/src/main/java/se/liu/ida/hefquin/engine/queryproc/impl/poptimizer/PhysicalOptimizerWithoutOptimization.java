package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public class PhysicalOptimizerWithoutOptimization extends PhysicalOptimizerBase
{
	@Override
	public boolean assumesLogicalMultiwayJoins() {
		return false;
	}

	@Override
	public boolean keepMultiwayJoinsInInitialPhysicalPlan() {
		return false;
	}

	@Override
	public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize(
			final PhysicalPlan initialPlan,
			final QueryProcContext ctxt )
					throws PhysicalOptimizationException
	{
		return new Pair<>( initialPlan, new PhysicalOptimizationStatsImpl() );
	}

}
