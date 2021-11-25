package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RewritingRule;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;
import se.liu.ida.hefquin.engine.utils.Pair;

public class IterativeImprovementBasedQueryOptimizer implements QueryOptimizer {
	
	protected final QueryOptimizationContext context;
	protected final StoppingConditionForIterativeImprovement condition;

	
	public IterativeImprovementBasedQueryOptimizer (final QueryOptimizationContext ctxt, final StoppingConditionForIterativeImprovement x) {
		assert ctxt != null;
		assert x != null;
		context = ctxt;
		condition = x;
	}
	
	
	@Override
	public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
		return optimize( context.getLogicalToPhysicalPlanConverter().convert(initialPlan,false) );
	}

	public PhysicalPlan optimize( final PhysicalPlan initialPlan ) throws QueryOptimizationException {
		// The best plan we have found so far. As we have only found one plan, it is the best one so far.
		PhysicalPlan bestPlan = initialPlan;
		PhysicalPlan currentPlan = bestPlan; // This variable will hold the plan which is currently being worked on.
		
		// Creating the completable future.
		CostModel costModel = context.getCostModel();
		CompletableFuture<Double> futureCost = costModel.initiateCostEstimation(bestPlan);

		// Get the cost.
		// currentCost = best cost among all neighbours. For it to be a local minimum, none of its neighbours can have a lower cost.
		Double currentCost;
		try {
			currentCost = futureCost.get();
		} catch(final ExecutionException e) {
			throw new QueryOptimizationException("CompletableFuture throws ExecutionException!", e);
		} catch(final InterruptedException e) {
			throw new QueryOptimizationException("CompletableFuture throws InterruptedException!", e);		
		}
		// bestCost = best possible cost out of everything that the algorithm has found.
		Double initialCost = currentCost;
		Double bestCost = currentCost;
		
		// Optimization takes place here.
		// Given the one starting state, find a local minimum.
		// Firstly, generate neighbours. (Come to think of it, should I use the British or American spelling of neighbours?)
		RewritingRule rwRule = null;
		List<PhysicalPlan> neighbours;
		
		Double[] neighbourCosts;
		List<Pair<PhysicalPlan,Double>> betterPlans = new ArrayList<>();
		Pair<PhysicalPlan,Double> betterPlan = null;
		Random rng = new Random();
		
		boolean improvementFound;
		int generation = 0;
		
		while(!condition.readyToStop(generation)) { // Currently only handles generation number as a stopping condition!
			
			// The randomized plan generator is to be used here. As a temporary measure, the initial plan is used.
			currentPlan = initialPlan;
			currentCost = initialCost;
			
			while(true) {
				generation += 1;
				
				neighbours = getNeighbours(currentPlan,rwRule);
				neighbourCosts = CostEstimationUtils.getEstimates(costModel, neighbours);
				improvementFound = false;
				
				// Using a for-loop in order to have the index for which neighbouring plan to pick.
				for (int x = 0; x < neighbourCosts.length; x++) {
					if(neighbourCosts[x] < currentCost) { // Since it's a list, iterating through the list once in the loop is probably better than using get, but I'm having this as a stop-gap measure until I've properly figured out Java lists.
						improvementFound = true;
						betterPlan = new Pair<PhysicalPlan, Double>(neighbours.get(x), neighbourCosts[x]);
						betterPlans.add(betterPlan); // I haven't figured out how to do this properly for Java yet, but it will be solved by the PhysicalPlanWithCost anyway.
					}
				}
				
				if(!improvementFound) {
					break; // break the loop if no improvement is found. This means that we have found a local minimum.
				} else {
					betterPlan = betterPlans.get(rng.nextInt(betterPlans.size())); // Get a random object.
					currentPlan = betterPlan.object1;
					currentCost = betterPlan.object2;
				}
			}
			
			if(currentCost < bestCost) {
				bestCost = currentCost;
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
