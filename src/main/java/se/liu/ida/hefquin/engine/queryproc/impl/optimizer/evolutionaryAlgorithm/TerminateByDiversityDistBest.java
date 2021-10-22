package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

/**
 * Diversity-based termination criterion:
 *
 * Similar to {@link TerminateByDiversityDistMax}, but only the top-k plans with lowest cost within each generation are considered.
 * Termination is triggered when the relative difference between the cost of the best plan
 * and the top-k best plan within each generation has not exceeded
 * a specified distance threshold for a number of generations.
 */
public class TerminateByDiversityDistBest implements TerminationCriterion
{
    protected final double distBestThreshold;
    protected final int topK;
    protected final int nrGenerations;

    public TerminateByDiversityDistBest( final double distBestThreshold, final int nrGenerations, final int topK ) {
        this.distBestThreshold = distBestThreshold;
        this.topK = topK;
        this.nrGenerations = nrGenerations;
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        final int previousGenerationNr = allPreviousGenerations.size();
        if ( previousGenerationNr + 1 < nrGenerations ) {
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

            final Generation previousGen = allPreviousGenerations.get( previousGenerationNr-nrGensForSteadyState );
            bestPlanCost = previousGen.bestPlan.getWeight();
            topKPlanCost = PhysicalPlanWithCostUtils.findTopKPlanWithLowestCost( previousGen.plans, topK ).getWeight();

            nrGensForSteadyState++;
        }

        return true;
    }

}
