package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.utils.Pair;

public class TwoPhaseQueryOptimizer extends RandomizedQueryOptimizerBase {
	
	protected final ConditionsForTwoPhaseOptimization conditions;
	protected final IterativeImprovementBasedQueryOptimizer optimizer1;
	protected final SimulatedAnnealing optimizer2;

	public TwoPhaseQueryOptimizer( final ConditionsForTwoPhaseOptimization x,
	                           final QueryOptimizationContext context,
	                           final RuleInstances rewritingRules) {
		super(context, rewritingRules);
		assert x != null;
		conditions = x;
		optimizer1 = new IterativeImprovementBasedQueryOptimizer(conditions.getStopping(), context, rewritingRules);
		optimizer2 = new SimulatedAnnealing(conditions.getEquilibrium(), context, rewritingRules);
	}

	@Override
	public Pair<PhysicalPlan, QueryOptimizationStats> optimize(LogicalPlan initialPlan)
			throws QueryOptimizationException {
		return optimizer2.optimize(optimizer1.optimize(initialPlan).object1,LogicalPlanUtils.countSubplans(initialPlan)); // Should work, but currently only gets the stats from the SA stage.
	}

}
