package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizationStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizerBase;

/**
 * This class implements a simple query optimizer that focuses only
 * on join ordering, for which it uses an enumeration algorithm to
 * optimize any subplan that consists of a group of joins.
 *
 * The concrete enumeration algorithm to be used for this purpose is
 * not hard-coded but, instead, can be specified by means of providing
 * an implementation of {@link JoinPlanOptimizer}.
 */
public class SimpleJoinOrderingQueryOptimizer extends PhysicalOptimizerBase
{
	protected final JoinPlanOptimizer joinPlanOptimizer;

	public SimpleJoinOrderingQueryOptimizer( final JoinPlanOptimizer joinPlanOptimizer ) {
		assert joinPlanOptimizer != null;
		this.joinPlanOptimizer = joinPlanOptimizer;
	}

	@Override
	public boolean assumesLogicalMultiwayJoins() {
		return true;
	}

	@Override
	public boolean keepMultiwayJoinsInInitialPhysicalPlan() {
		return true;
	}

	@Override
	public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize(
			final PhysicalPlan initialPlan,
			final QueryProcContextExt ctx )
					throws PhysicalOptimizationException
	{
		return new Pair<>( optimizePlan(initialPlan, ctx),
		                   new PhysicalOptimizationStatsImpl() );
	}

	public PhysicalPlan optimizePlan( final PhysicalPlan plan,
	                                  final QueryProcContextExt ctx )
			throws PhysicalOptimizationException
	{
		if ( plan.numberOfSubPlans() == 0 ) {
			return plan;
		}

		final PhysicalPlan[] optSubPlans = getOptimizedSubPlans(plan, ctx);

		if ( hasMultiwayJoinAsRoot(plan) ){
			return joinPlanOptimizer.determineJoinPlan(optSubPlans, ctx);
		}
		else {
			return PhysicalPlanFactory.createPlan( plan.getRootOperator(), optSubPlans );
		}
	}

	protected PhysicalPlan[] getOptimizedSubPlans( final PhysicalPlan plan,
	                                               final QueryProcContextExt ctx )
			throws PhysicalOptimizationException
	{
		final int numChildren = plan.numberOfSubPlans();
		final PhysicalPlan[] children = new PhysicalPlan[numChildren];
		for ( int i = 0; i < numChildren; ++i ) {
			children[i] = optimizePlan( plan.getSubPlan(i), ctx );
		}
		return children;
	}

	protected boolean hasMultiwayJoinAsRoot( final PhysicalPlan plan ) {
		final PhysicalOperator rootPOP = plan.getRootOperator();
		if ( rootPOP instanceof PhysicalOperatorForLogicalOperator rootPOP2 ) {
			final LogicalOperator rootLOP = rootPOP2.getLogicalOperator();
			return rootLOP instanceof LogicalOpMultiwayJoin;
		}

		throw new IllegalArgumentException( rootPOP.getClass().getName() );
	}

}
