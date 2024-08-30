package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizationStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple.RandomizedJoinPlanOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple.SimpleJoinOrderingQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCostUtils;

public class IterativeImprovementBasedQueryOptimizer extends RandomizedQueryOptimizerBase
{
	protected final StoppingConditionForIterativeImprovement condition;
	protected final SimpleJoinOrderingQueryOptimizer simpleOptimizer;

	public IterativeImprovementBasedQueryOptimizer( final StoppingConditionForIterativeImprovement x,
	                                                final LogicalToPhysicalPlanConverter l2pConverter,
	                                                final CostModel costModel,
	                                                final RuleInstances rewritingRules ) {
		super(l2pConverter, costModel, rewritingRules);

		assert x != null;
		condition = x;
		final RandomizedJoinPlanOptimizerImpl randomizer = new RandomizedJoinPlanOptimizerImpl();
		simpleOptimizer = new SimpleJoinOrderingQueryOptimizer(randomizer, l2pConverter);
	}

	@Override
	public boolean assumesLogicalMultiwayJoins() {
		return true;
	}

	@Override
	public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final LogicalPlan initialPlan ) throws PhysicalOptimizationException {
		return optimize( l2pConverter.convert(initialPlan,true) );
	}

	public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final PhysicalPlan initialPlan ) throws PhysicalOptimizationException {
		// The best plan and cost we have found so far. As we have only found one plan, it is the best one so far.
		PhysicalPlanWithCost bestPlan = PhysicalPlanWithCostUtils.annotatePlanWithCost(costModel, initialPlan);

		// generation = number of times the outer loop has run. Has to be declared here since it will increment each outer loop.
		int generation = 0;

		while ( ! condition.readyToStop(generation) ) {
			// Selects a random plan and uses it as the starting point for the optimization.
			PhysicalPlan randomPlan = simpleOptimizer.optimizePlan(initialPlan);
			PhysicalPlanWithCost currentPlan = PhysicalPlanWithCostUtils.annotatePlanWithCost(costModel, randomPlan);
			
			boolean improvementFound = false;

			do {
				final List<PhysicalPlanWithCost> neighbours = PhysicalPlanWithCostUtils.annotatePlansWithCost( costModel, getNeighbours(currentPlan.getPlan()) );
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

		final PhysicalOptimizationStats myStats = new PhysicalOptimizationStatsImpl();

		return new Pair<>( bestPlan.getPlan(), myStats );
	}

}
