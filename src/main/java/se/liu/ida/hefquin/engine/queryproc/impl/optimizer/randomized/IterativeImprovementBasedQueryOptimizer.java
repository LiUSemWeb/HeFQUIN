package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.RandomizedJoinPlanOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.SimpleJoinOrderingQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCostUtils;
import se.liu.ida.hefquin.engine.utils.Pair;

public class IterativeImprovementBasedQueryOptimizer extends RandomizedQueryOptimizerBase
{
	protected final StoppingConditionForIterativeImprovement condition;
	protected final SimpleJoinOrderingQueryOptimizer simpleOptimizer;

	public IterativeImprovementBasedQueryOptimizer( final StoppingConditionForIterativeImprovement x,
	                                                final QueryOptimizationContext context,
	                                                final RuleInstances rewritingRules ) {
		super(context, rewritingRules);

		assert x != null;
		condition = x;
		final RandomizedJoinPlanOptimizerImpl randomizer = new RandomizedJoinPlanOptimizerImpl();
		simpleOptimizer = new SimpleJoinOrderingQueryOptimizer(randomizer,context);
	}

	@Override
	public Pair<PhysicalPlan, QueryOptimizationStats> optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
		return optimize( context.getLogicalToPhysicalPlanConverter().convert(initialPlan,true) );
	}

	public Pair<PhysicalPlan, QueryOptimizationStats> optimize( final PhysicalPlan initialPlan ) throws QueryOptimizationException {
		// The best plan and cost we have found so far. As we have only found one plan, it is the best one so far.
		PhysicalPlanWithCost bestPlan = PhysicalPlanWithCostUtils.annotatePlanWithCost( context.getCostModel(), initialPlan );

		// generation = number of times the outer loop has run. Has to be declared here since it will increment each outer loop.
		int generation = 0;

		while ( ! condition.readyToStop(generation) ) {
			// Selects a random plan and uses it as the starting point for the optimization.
			PhysicalPlan randomPlan = simpleOptimizer.optimizePlan(initialPlan);
			PhysicalPlanWithCost currentPlan = PhysicalPlanWithCostUtils.annotatePlanWithCost( context.getCostModel(), randomPlan );
			
			boolean improvementFound = false;

			do {
				final List<PhysicalPlanWithCost> neighbours = PhysicalPlanWithCostUtils.annotatePlansWithCost( context.getCostModel(), getNeighbours(currentPlan.getPlan()));
				final List<PhysicalPlanWithCost> betterPlans = new ArrayList<>();
				improvementFound = false;

				// Using a for-loop in order to have the index for which neighbouring plan to pick.
				for ( int x = 0; x < neighbours.size(); x++ ) {
					if( neighbours.get(x).getWeight() < currentPlan.getWeight() ) {
						improvementFound = true;
						betterPlans.add( neighbours.get(x) );
					}
				}

				if ( improvementFound ) { // if we have found at least one possible improvement, we want to make one of them into our new current plan.
					currentPlan = getRandomElement(betterPlans); // Get a random object.
				}
			}
			while ( improvementFound );

			if ( currentPlan.getWeight() < bestPlan.getWeight() ) {
				bestPlan = currentPlan;
			}

			generation++;
		}

		final QueryOptimizationStats myStats = new QueryOptimizationStatsImpl();

		return new Pair<>( bestPlan.getPlan(), myStats );
	}

}
