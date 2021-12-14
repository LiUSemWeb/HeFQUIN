package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCost;

public class SimulatedAnnealing extends RandomizedQueryOptimizerBase {

	public SimulatedAnnealing(	final QueryOptimizationContext context,
								final RuleInstances rewritingRules) {
		super(context, rewritingRules);
	}

	@Override
	public PhysicalPlan optimize(PhysicalPlan initialPlan) throws QueryOptimizationException {
		
		// The first plan, which is currently the best plan we know of.
		PhysicalPlanWithCost bestPlan = new PhysicalPlanWithCost(initialPlan, CostEstimationUtils.getEstimates( context.getCostModel(), initialPlan )[0]);
		PhysicalPlanWithCost currentPlan = bestPlan;
		
		// A temperature value is assigned based on the cost. Because of how this value is used, the only important thing is its size relative to the cost of the current plan.
		double temperature = currentPlan.getWeight()*2;
		int unchanged = 0;
		
		// Possible TODO: Define what frozen means. Whether to hardcode a temp threshhold below which is frozen, or to pass on a frozen threshhold to the constructor, or something else entirely.
		// I'm using the paper's implementation of frozen for now.
		// While not frozen, do
		while(!isFrozen(temperature,unchanged)) {
			
			unchanged++;
			
			// TODO: Define what equilibrium entails on a practical level
			// The paper's implementation counts equilibrium as 16*(number of joins in query).
			while(!isEquilibrium()) {
			
				// Here we want a random neighbour, and its cost
				final PhysicalPlan temporaryPlan = getRandom(getNeighbours(currentPlan.getPlan()));
				final PhysicalPlanWithCost alternativePlan = new PhysicalPlanWithCost(temporaryPlan, CostEstimationUtils.getEstimates( context.getCostModel(), temporaryPlan )[0]);
				// Alternatively, a better option is possibly to create a new constructor for PhysicalPlanWithCost...
				// ...that only takes a PhysicalPlan argument, and gets the cost itself.
				
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
	
	protected boolean isFrozen(double temperature, int unchanged) {
		 // This is the paper's example frozen condition. Low temp and best is left unchanged for 4 outer loops.
		return (temperature < 1 && unchanged >= 4);
	}
	
}
