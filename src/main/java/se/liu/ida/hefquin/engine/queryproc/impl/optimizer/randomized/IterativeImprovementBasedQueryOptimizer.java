package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import java.util.ArrayList;
import java.util.List;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.RandomizedJoinPlanOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCost;

public class IterativeImprovementBasedQueryOptimizer extends RandomizedQueryOptimizerBase
{
	protected final StoppingConditionForIterativeImprovement condition;

	public IterativeImprovementBasedQueryOptimizer( final StoppingConditionForIterativeImprovement x,
	                                                final QueryOptimizationContext context,
	                                                final RuleInstances rewritingRules ) {
		super(context, rewritingRules);

		assert x != null;
		condition = x;
	}

	@Override
	public PhysicalPlan optimize( final PhysicalPlan initialPlan ) throws QueryOptimizationException {
		// The best plan we have found so far. As we have only found one plan, it is the best one so far.
		PhysicalPlan bestPlan = initialPlan;

		final Double initialCost = CostEstimationUtils.getEstimates( context.getCostModel(), initialPlan )[0];

		// generation = number of times the outer loop has run. Has to be declared here since it will increment each outer loop.
		int generation = 0;

		// bestCost = best possible cost out of everything that the algorithm has found. Has to be declared here so that we have something to compare to in all the iterations of the outer loop without losing track of it between loops.
		Double bestCost = initialCost;

		while ( !condition.readyToStop(generation) ) { // Currently only handles generation number as a stopping condition!
			// The randomized plan generator is to be used here. As a temporary measure, the initial plan is used.
			/*
			PhysicalPlan currentPlan = initialPlan; // This variable will hold the plan which is currently being worked on.
			*/
			final RandomizedJoinPlanOptimizerImpl planRandomizer = new RandomizedJoinPlanOptimizerImpl();
			PhysicalPlan currentPlan = planRandomizer.determineJoinPlan(getNeighbours(initialPlan));
			
			Double currentCost = CostEstimationUtils.getEstimates( context.getCostModel(), currentPlan )[0]; // The cost of the current plan. For it to be a local minimum, none of its neighbours can have a lower cost
			boolean improvementFound = false;

			do {
				final List<PhysicalPlan> neighbours = getNeighbours(currentPlan);
				final Double[] neighbourCosts = CostEstimationUtils.getEstimates( context.getCostModel(), neighbours );
				final List<PhysicalPlanWithCost> betterPlans = new ArrayList<>();
				improvementFound = false;

				// Using a for-loop in order to have the index for which neighbouring plan to pick.
				for (int x = 0; x < neighbourCosts.length; x++) {
					if(neighbourCosts[x] < currentCost) {
						improvementFound = true;
						final PhysicalPlanWithCost betterPlan = new PhysicalPlanWithCost(neighbours.get(x), neighbourCosts[x]);
						betterPlans.add(betterPlan); // I haven't figured out how to do this properly for Java yet, but it will be solved by the PhysicalPlanWithCost anyway.
					}
				}

				if(improvementFound) { // if we have found at least one possible improvement, we want to make one of them into our new current plan.
					final PhysicalPlanWithCost newPlan = getRandom(betterPlans); // Get a random object.
					currentPlan = newPlan.getPlan();
					currentCost = newPlan.getWeight();
				}
			}
			while(improvementFound);

			if(currentCost < bestCost) {
				bestCost = currentCost;
				bestPlan = currentPlan;
			}

			generation++;
		}

		return bestPlan;
	}

}
