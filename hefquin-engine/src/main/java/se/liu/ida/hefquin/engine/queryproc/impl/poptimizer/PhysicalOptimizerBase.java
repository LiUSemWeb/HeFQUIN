package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public abstract class PhysicalOptimizerBase implements PhysicalOptimizer
{
	private static final Logger log = LoggerFactory.getLogger( PhysicalOptimizerBase.class );

	@Override
	public final Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final LogicalPlan lp,
	                                                                     final QueryProcContextExt ctx )
			throws PhysicalOptimizationException {
		final boolean keepMultiwayJoins = keepMultiwayJoinsInInitialPhysicalPlan();

		log.debug( "Converting logical plan to physical plan (keepMultiwayJoins={})", keepMultiwayJoins );

		final LogicalToPhysicalPlanConverter lp2pp = ctx.getLogicalToPhysicalPlanConverter();
		final PhysicalPlan initialPhysicalPlan = lp2pp.convert( lp,
		                                                        keepMultiwayJoins,
		                                                        ctx.getLogicalToPhysicalOpConverter() );

		return optimize(initialPhysicalPlan, ctx);
	}

	/**
	 * Return true if this optimizer expects that multiway joins are carried
	 * over from the given logical plan into the initial physical plan. The
	 * {@link #optimize(LogicalPlan)} function passes this flag as the second
	 * argument of the
	 * {@link LogicalToPhysicalPlanConverter#convert(LogicalPlan, boolean)}
	 * function.
	 */
	protected abstract boolean keepMultiwayJoinsInInitialPhysicalPlan();

	protected abstract Pair<PhysicalPlan, PhysicalOptimizationStats> optimize(
			PhysicalPlan initialPhysicalPlan,
			QueryProcContextExt ctx ) throws PhysicalOptimizationException;
}
