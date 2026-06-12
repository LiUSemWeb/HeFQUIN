package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public class PhysicalOptimizerWithoutOptimization extends PhysicalOptimizerBase
{
	private static final Logger log = LoggerFactory.getLogger( PhysicalOptimizerWithoutOptimization.class );

	@Override
	public boolean assumesLogicalMultiwayJoins() {
		return false;
	}

	@Override
	public boolean keepMultiwayJoinsInInitialPhysicalPlan() {
		return false;
	}

	@Override
	protected Pair<PhysicalPlan, PhysicalOptimizationStats> optimize(
			final PhysicalPlan initialPlan,
			final QueryProcContextExt ctx )
					throws PhysicalOptimizationException
	{
		log.debug( "Skipping physical optimization (identity optimizer)." );
		return new Pair<>( initialPlan, new PhysicalOptimizationStatsImpl() );
	}

}
