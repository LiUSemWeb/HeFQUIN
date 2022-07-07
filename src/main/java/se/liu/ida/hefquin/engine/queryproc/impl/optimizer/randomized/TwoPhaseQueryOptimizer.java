package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.utils.Pair;

public class TwoPhaseQueryOptimizer extends RandomizedQueryOptimizerBase {
	protected final IterativeImprovementBasedQueryOptimizer optimizer1;
	protected final SimulatedAnnealing optimizer2;

	public TwoPhaseQueryOptimizer( final StoppingConditionForIterativeImprovement condition1,
								final EquilibriumConditionForSimulatedAnnealing condition2,
	                           final QueryOptimizationContext context,
	                           final RuleInstances rewritingRules) {
		super(context, rewritingRules);
		optimizer1 = new IterativeImprovementBasedQueryOptimizer(condition1, context, rewritingRules);
		optimizer2 = new SimulatedAnnealing(condition2, context, rewritingRules);
	}

	@Override
	public Pair<PhysicalPlan, QueryOptimizationStats> optimize( final LogicalPlan initialPlan )
			throws QueryOptimizationException {
		final PhysicalPlan halfwayPlan = optimizer1.optimize(initialPlan).object1;
		final PhysicalPlan finalPlan = optimizer2.optimize(halfwayPlan,LogicalPlanUtils.countSubplans(initialPlan),0.1).object1;

		final QueryOptimizationStats myStats = new QueryOptimizationStatsImpl();

		return new Pair<>( finalPlan, myStats );
	}

}
