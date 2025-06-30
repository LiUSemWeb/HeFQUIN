package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizationStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCostUtils;

public class SimulatedAnnealing extends RandomizedQueryOptimizerBase
{
	protected final EquilibriumConditionForSimulatedAnnealing condition;

	public SimulatedAnnealing( final EquilibriumConditionForSimulatedAnnealing x,
	                           final LogicalToPhysicalPlanConverter l2pConverter,
	                           final CostModel costModel,
	                           final RuleInstances rewritingRules) {
		super(l2pConverter, costModel, rewritingRules);
		condition = x;
	}

	@Override
	public boolean assumesLogicalMultiwayJoins() {
		return false;
	}

	@Override
	public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final LogicalPlan initialPlan ) throws PhysicalOptimizationException {
		final PhysicalPlan initialPP = l2pConverter.convert(initialPlan,false);
		final int numberOfSubplans = LogicalPlanUtils.countSubplans(initialPlan);
		return optimize(initialPP, numberOfSubplans);
	}


	public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final PhysicalPlan initialPlan,
	                                                            final int numberOfSubplans ) throws PhysicalOptimizationException {
		return optimize(initialPlan, numberOfSubplans, 2.0);
	}

	public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final PhysicalPlan initialPlan,
	                                                            final int numberOfSubplans,
	                                                            final double temperatureModifier) throws PhysicalOptimizationException {
		// The first plan, which is currently the best plan we know of.
		PhysicalPlanWithCost bestPlan = PhysicalPlanWithCostUtils.annotatePlanWithCost(costModel, initialPlan);
		PhysicalPlanWithCost currentPlan = bestPlan;

		// A temperature value is assigned based on the cost. Because of how this value is used, the only important thing is its size relative to the cost of the current plan.
		double temperature = currentPlan.getWeight() * temperatureModifier;
		int unchanged = 0;

		// While not frozen, do
		while( ! isFrozen(temperature,unchanged) ) {
			unchanged++;
			int generation = 0; // Generation counter for inner loop.

			// While not in equilibrium, do
			while( ! condition.isEquilibrium(generation, numberOfSubplans) ) {
				// Here we want a random neighbour, and its cost
				final PhysicalPlan temporaryPlan = getRandomElement(getNeighbours(currentPlan.getPlan()));
				final PhysicalPlanWithCost alternativePlan = PhysicalPlanWithCostUtils.annotatePlanWithCost(costModel, temporaryPlan);

				final double costDelta = currentPlan.getWeight() - alternativePlan.getWeight();
				if ( costDelta <= 0 ) {
					currentPlan = alternativePlan;
				}
				else if ( rng.nextDouble() > Math.exp(-costDelta/temperature) ) {
					currentPlan = alternativePlan;
				}

				if ( currentPlan.getWeight() < bestPlan.getWeight() ) {
					bestPlan = currentPlan;
					unchanged = 0;
				}

				generation++;
			}

			// reduce temperature. Using the example setting from the paper.
			temperature *= 0.95;
		}

		final PhysicalOptimizationStats myStats = new PhysicalOptimizationStatsImpl();

		return new Pair<>( bestPlan.getPlan(), myStats );
	}

	protected boolean isFrozen( final double temperature, final int unchanged ) {
		 // This is the paper's example frozen condition. Low temp and best is left unchanged for 4 outer loops.
		return (temperature < 1) && (unchanged >= 4);
	}

}
