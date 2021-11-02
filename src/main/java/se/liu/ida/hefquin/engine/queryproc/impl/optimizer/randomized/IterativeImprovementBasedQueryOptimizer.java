package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RewritingRule;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class IterativeImprovementBasedQueryOptimizer implements QueryOptimizer {
	
	protected final QueryOptimizationContext context;
	
	public IterativeImprovementBasedQueryOptimizer (final QueryOptimizationContext ctxt) {
		assert ctxt != null;
		context = ctxt;
	}
	
	
	@Override
	public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {

		final LogicalToPhysicalPlanConverter converter = context.getLogicalToPhysicalPlanConverter();

		PhysicalPlan optimalPlan = converter.convert(initialPlan, false);
		
		// Creating the completable future.
		CostModel costModel = context.getCostModel();
		CompletableFuture<Double> futureCost = costModel.initiateCostEstimation(optimalPlan);
		
		// Create rule instances.

		// Get the cost.
		Double localMinimum;
		try {
			localMinimum = futureCost.get();
		} catch(final ExecutionException e) {
			throw new QueryOptimizationException("CompletableFuture throws ExecutionException!", e);
		} catch(final InterruptedException e) {
			throw new QueryOptimizationException("CompletableFuture throws InterruptedException!", e);		
		}
		Double optimalCost, currentCost = localMinimum;
		// currentCost = cost of the plan whose neighbours is being looked at.
		// localMinimum = best cost among all neighbours
		// optimalCost = best possible cost out of everything that the algorithm has found.
		
		// Optimization takes place here.
		// Given the one starting state, find a local minimum.
		// Firstly, generate neighbours. (Come to think of it, should I use the British or American spelling of neighbours?)
		RewritingRule rwRule = null; // Eclipse suggested initiating this by adding the "= null". This doesn't seem quite right...
		List<PhysicalPlan> neighbours = getNeighbours(optimalPlan,rwRule);
		
		List<CompletableFuture<Double>> futureNeighbourCosts = new ArrayList<>();
		List<Double> neighbourCosts;
		
		while(true) {
			for(int i = 0; i < neighbours.size(); i++ ) {
				// Here, I want to get an iterator for the first element of the list, and iterate through the list and get initiateCostEstimations for each one, filling up the futureNeighbourCosts
			}
			// Here, I want to have a second loop which gets the costs of the neighbours and compares them to the cost of the current plan looked at.
			// If no neighbour beats the current plan, this part of the algorithm is over and the loop breaks.
			// If any neighbour beats the current plan, the best neighbour becomes the new current plan.
			
			break; // Temporary break to not yield an error.
		}
		
		return optimalPlan;
	}
	
	/**
	 * Currently created as a member method, but I might end up moving this to somewhere else if I find that it is needed in more places.
	 * That obviously depends on whether Java supports importing methods outside of objects.
	 * In C++, I would be passing references here - is something similar possible/appropriate in Java?
	 * If significant performance is lost by making this a separate function, I'll be baking it into optimize. I've just been taught so far at LiU that making separate smaller functions is preferable so that the code becomes a bit more readable.
	 * @return
	 */
	protected List<PhysicalPlan> getNeighbours(final PhysicalPlan initialPlan, final RewritingRule rewritingRule) {
		List<PhysicalPlan> resultList = new ArrayList<PhysicalPlan>(); // Create an empty list. Just List<PhysicalPlan> didn't work so I had to look this up.
		
		Set<RuleApplication> ruleApplications = rewritingRule.determineAllPossibleApplications(initialPlan);
		
		for ( final RuleApplication ra : ruleApplications ) {
			resultList.add( ra.getResultingPlan() );
		}
		
		return resultList;
	}
}
