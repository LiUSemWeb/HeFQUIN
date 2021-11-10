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
	protected final int iterations;
	
	public IterativeImprovementBasedQueryOptimizer (final QueryOptimizationContext ctxt, final int x) {
		assert ctxt != null;
		context = ctxt;
		iterations = x;
	}
	
	
	@Override
	public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {

		final LogicalToPhysicalPlanConverter converter = context.getLogicalToPhysicalPlanConverter();

		// The best plan we have found so far. As we have only found one plan, it is the best one so far.
		PhysicalPlan bestPlan = converter.convert(initialPlan, false);
		PhysicalPlan currentPlan = bestPlan; // This variable will hold the plan which is currently being worked on.
		
		// Creating the completable future.
		CostModel costModel = context.getCostModel();
		CompletableFuture<Double> futureCost = costModel.initiateCostEstimation(bestPlan);

		// Get the cost.
		// localMinimum = best cost among all neighbours. For it to be a local minimum, none of its neighbours can have a lower cost.
		Double localMinimum;
		try {
			localMinimum = futureCost.get();
		} catch(final ExecutionException e) {
			throw new QueryOptimizationException("CompletableFuture throws ExecutionException!", e);
		} catch(final InterruptedException e) {
			throw new QueryOptimizationException("CompletableFuture throws InterruptedException!", e);		
		}
		// bestCost = best possible cost out of everything that the algorithm has found.
		Double bestCost = localMinimum;
		
		// Optimization takes place here.
		// Given the one starting state, find a local minimum.
		// Firstly, generate neighbours. (Come to think of it, should I use the British or American spelling of neighbours?)
		RewritingRule rwRule = null;
		List<PhysicalPlan> neighbours;
		
		List<CompletableFuture<Double>> futureNeighbourCosts;
		List<Double> neighbourCosts;
		
		boolean improvementFound;
		
		for(int y = 0; y < iterations; y++) {
			
			// assigning a """random""" starting position to currentPlan is to take place here.
			
			while(true) {
				neighbours = getNeighbours(currentPlan,rwRule);
				futureNeighbourCosts = new ArrayList<>();
				neighbourCosts = new ArrayList<>();
				improvementFound = false;
				
				for (final PhysicalPlan pp : neighbours) {
					futureNeighbourCosts.add(costModel.initiateCostEstimation(pp));
				} // doing a similar loop to the one you showed be for RuleApplications. Does this guarantee that it will be in order?
				
				// instead of adding it in the same loop, another loop will be used to get the cost. This should improve performance due to multithreading.
				for (final CompletableFuture<Double> cf : futureNeighbourCosts) {
					try {
						neighbourCosts.add(cf.get());
					} catch(final ExecutionException e) {
						throw new QueryOptimizationException("CompletableFuture throws ExecutionException!", e);
					} catch(final InterruptedException e) {
						throw new QueryOptimizationException("CompletableFuture throws InterruptedException!", e);		
					}
				}
				
				// Using a for-loop in order to have the index for which neighbouring plan to pick.
				for (int x = 0; x < neighbourCosts.size(); x++) {
					if(neighbourCosts.get(x) < localMinimum) { // Since it's a list, iterating through the list once in the loop is probably better than using get, but I'm having this as a stop-gap measure until I've properly figured out Java lists.
						improvementFound = true;
						currentPlan = neighbours.get(x);
						localMinimum = neighbourCosts.get(x);
					}
				}
				
				if(!improvementFound) {
					break; // break the loop if no improvement is found. This means that we have found a local minimum.
				}
			}
			
			if(localMinimum < bestCost) {
				bestCost = localMinimum;
				bestPlan = currentPlan;
			}
		}
		
		return bestPlan;
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
