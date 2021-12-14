package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCostUtils;

public class SimulatedAnnealing extends RandomizedQueryOptimizerBase {

	public SimulatedAnnealing(	final QueryOptimizationContext context,
								final RuleInstances rewritingRules) {
		super(context, rewritingRules);
	}

	@Override
	public PhysicalPlan optimize( final PhysicalPlan initialPlan ) throws QueryOptimizationException {
		
		// The first plan, which is currently the best plan we know of.
		PhysicalPlanWithCost bestPlan = new PhysicalPlanWithCost(initialPlan, CostEstimationUtils.getEstimates( context.getCostModel(), initialPlan )[0]);
		PhysicalPlanWithCost currentPlan = bestPlan;
		
		// A temperature value is assigned based on the cost. Because of how this value is used, the only important thing is its size relative to the cost of the current plan.
		double temperature = currentPlan.getWeight()*2;
		int unchanged = 0;
		
		// While not frozen, do
		while(!isFrozen(temperature,unchanged)) {
			
			unchanged++;
			
			// TODO: Implement Equilibrium. Keep a counter and run the inner loop 16 times the number of logical operations in the initial logical plan.
			while(!isEquilibrium()) {
			
				// Here we want a random neighbour, and its cost
				final PhysicalPlan temporaryPlan = getRandomElement(getNeighbours(currentPlan.getPlan()));
				final PhysicalPlanWithCost alternativePlan = PhysicalPlanWithCostUtils.annotatePlanWithCost( context.getCostModel(), temporaryPlan );
				
				final double costDelta = currentPlan.getWeight() - alternativePlan.getWeight();
				if (costDelta <= 0) {
					currentPlan = alternativePlan;
				} else {
					if (rng.nextDouble() > Math.exp(-costDelta / temperature)) {
						currentPlan = alternativePlan;
					}
					
				}
				
				if(currentPlan.getWeight() < bestPlan.getWeight()) {
					bestPlan = currentPlan;
					unchanged = 0;
				}
			
			}
			
			// reduce temperature. Using the example setting from the paper.
			temperature *= 0.95;
		}
		
		return bestPlan.getPlan();
	}
	
	protected boolean isEquilibrium() {
		return false;
	}
	
	protected boolean isFrozen( final double temperature, final int unchanged ) {
		 // This is the paper's example frozen condition. Low temp and best is left unchanged for 4 outer loops.
		return (temperature < 1 && unchanged >= 4);
	}
	
}
