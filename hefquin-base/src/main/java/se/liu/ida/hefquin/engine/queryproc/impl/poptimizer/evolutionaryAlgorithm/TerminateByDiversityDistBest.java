package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCostUtils;

import java.util.List;

/**
 * Diversity-based termination criterion:
 *
 * Similar to {@link TerminateByDiversityDistMax}, but only the top-k plans with lowest cost within each generation are considered.
 * Termination is triggered when the relative difference between the cost of the best plan
 * and the top-k best plan within each generation has not exceeded
 * a specified distance threshold for a number of generations.
 */
public class TerminateByDiversityDistBest extends TerminationCriterionBase
{
	public static TerminationCriterionFactory getFactory( final double distBestThreshold, final double topK ) {
		return new TerminationCriterionFactory() {
			@Override public TerminationCriterion createInstance( final LogicalPlan plan ) {
				return new TerminateByDiversityDistBest(distBestThreshold, topK, plan);
			}
		};
	}


    protected final double distBestThreshold;
    protected final double topK;

    public TerminateByDiversityDistBest( final double distBestThreshold, final double topK, final LogicalPlan plan ) {
        super(plan);
        this.distBestThreshold = distBestThreshold;
        this.topK = topK;
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        final int previousGenerationNr = allPreviousGenerations.size();
        if ( previousGenerationNr < nrGenerations ) {
            return false;
        }

        double bestPlanCost = currentGeneration.bestPlan.getWeight();
        double topKPlanCost = PhysicalPlanWithCostUtils.findTopKPlanWithLowestCost( currentGeneration.plans, topK ).getWeight();

        int nrGensForSteadyState = 0;
        while ( nrGensForSteadyState < nrGenerations ) {
            final double relTopKDifference = ( topKPlanCost - bestPlanCost ) / topKPlanCost;
            if ( relTopKDifference > distBestThreshold ) {
                return false;
            }

            nrGensForSteadyState++;
            final Generation previousGen = allPreviousGenerations.get( previousGenerationNr-nrGensForSteadyState );
            bestPlanCost = previousGen.bestPlan.getWeight();
            topKPlanCost = PhysicalPlanWithCostUtils.findTopKPlanWithLowestCost( previousGen.plans, topK ).getWeight();
        }

        return true;
    }

}
