package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;

public class IterativeImprovement implements QueryOptimizer {
	
	QueryOptimizationContext context;
	LogicalToPhysicalPlanConverter converter;
	CostModel costModel;
	
	public IterativeImprovement (final QueryOptimizationContext ctxt) {
		assert ctxt != null;
		context = ctxt;
		converter = context.getLogicalToPhysicalPlanConverter();
		costModel = context.getCostModel();
	}
	
	
	@Override
	public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
		PhysicalPlan optimalPlan = converter.convert(initialPlan, false); // I don't recall anything about multiway joins, so I assume the keep multiway joins boolean is to be false.
		
		// Creating the completable future.
		CompletableFuture<Double> futureCost = costModel.initiateCostEstimation(optimalPlan);
		
		// Create rule instances.

		// Get the cost.
		Double optimalCost;
		try {
			optimalCost = futureCost.get();
		} catch(ExecutionException e1) {
			throw new QueryOptimizationException("CompletableFuture throws ExecutionException!");
		} catch(InterruptedException e2) {
			throw new QueryOptimizationException("CompletableFuture throws InterruptedException!");
			
		}
		
		// Optimization takes place here.
		
		return optimalPlan;
	}
}
