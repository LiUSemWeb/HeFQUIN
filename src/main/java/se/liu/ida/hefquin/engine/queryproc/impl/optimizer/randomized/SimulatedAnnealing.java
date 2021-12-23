package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCostUtils;

public class SimulatedAnnealing extends RandomizedQueryOptimizerBase {
	
	protected final EquilibriumConditionForSimulatedAnnealing condition;

	public SimulatedAnnealing(	final EquilibriumConditionForSimulatedAnnealing x,
								final QueryOptimizationContext context,
								final RuleInstances rewritingRules) {
		super(context, rewritingRules);
		condition = x;
	}


	@Override
	public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
		final PhysicalPlan initialPP = context.getLogicalToPhysicalPlanConverter().convert(initialPlan,false);
		final int numberOfSubplans = LogicalPlanUtils.countSubplans(initialPlan);
		return optimize(initialPP, numberOfSubplans);
	}
	
	public PhysicalPlan optimize( final PhysicalPlan initialPlan, final int numberOfSubplans ) throws QueryOptimizationException {
		
		// The first plan, which is currently the best plan we know of.
		PhysicalPlanWithCost bestPlan = PhysicalPlanWithCostUtils.annotatePlanWithCost( context.getCostModel(), initialPlan );
		PhysicalPlanWithCost currentPlan = bestPlan;
		
		// A temperature value is assigned based on the cost. Because of how this value is used, the only important thing is its size relative to the cost of the current plan.
		double temperature = currentPlan.getWeight()*2;
		int unchanged = 0;
		
		// While not frozen, do
		while(!isFrozen(temperature,unchanged)) {
			
			unchanged++;
			int generation = 0; // Generation counter for inner loop.
			
			// While not in equilibrium, do
			while(!condition.isEquilibrium(generation, numberOfSubplans)) {
			
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
			
				generation++;
			}
			
			// reduce temperature. Using the example setting from the paper.
			temperature *= 0.95;
		}
		
		return bestPlan.getPlan();
	}
	
	protected boolean isFrozen( final double temperature, final int unchanged ) {
		 // This is the paper's example frozen condition. Low temp and best is left unchanged for 4 outer loops.
		return (temperature < 1 && unchanged >= 4);
	}
	
}
