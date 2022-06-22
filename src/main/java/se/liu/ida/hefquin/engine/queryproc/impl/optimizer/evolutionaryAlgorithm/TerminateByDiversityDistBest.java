package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCostUtils;

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
    protected final double topK;
    protected int nrGenerations;

    public TerminateByDiversityDistBest( final double distBestThreshold, final double topK ) {
        this.distBestThreshold = distBestThreshold;
        this.topK = topK;
    }

    @Override
    public void initialize( final LogicalPlan plan ){
        this.nrGenerations = InitializeNrGeneration.countNumOfOp(plan);
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
