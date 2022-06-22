package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

import java.util.List;

/**
 * Distance-based termination criterion:
 *
 * Termination is triggered when the relative distance between the average cost
 * in the current generation and in the previous generation has not exceeded
 * a specified distance threshold for a number of generations.
 * (The nrGenerations is a dynamic number depending on the number of lowest-cost plans).
 */
public class TerminateByDistancePercAvgDynamicG implements TerminationCriterion
{
    protected final double percAvgThreshold;
    protected int nrGenerations;

    public TerminateByDistancePercAvgDynamicG( final double percAvgThreshold ) {
        this.percAvgThreshold = percAvgThreshold;
    }

    @Override
    public void initialize( final LogicalPlan plan ){
        this.nrGenerations = InitializeNrGeneration.countNumOfOp(plan);
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        final int previousGenerationNr = allPreviousGenerations.size();
        if ( previousGenerationNr < 1 ) {
            return false;
        }

//      Update nrGenerations
        double percentageNrOfPlansWithBestCost = currentGeneration.nrOfPlansWithBestCost / (double) allPreviousGenerations.get( previousGenerationNr-1 ).nrOfPlansWithBestCost;
        nrGenerations = (int) Math.ceil( nrGenerations / percentageNrOfPlansWithBestCost);
        if ( previousGenerationNr < nrGenerations ) {
            return false;
        }

        double avgCurrentCost = currentGeneration.avgCost;
        double avgPreviousCost;
        double relDistance;
        Generation previousGen;

        int nrGensForSteadyState = 0;
        while ( nrGensForSteadyState < nrGenerations ) {
            previousGen = allPreviousGenerations.get( previousGenerationNr-nrGensForSteadyState-1 );
            avgPreviousCost = previousGen.avgCost;
            relDistance = ( avgPreviousCost - avgCurrentCost ) / avgPreviousCost;
            if ( relDistance > percAvgThreshold ) {
                return false;
            }

            avgCurrentCost = avgPreviousCost;
            nrGensForSteadyState ++;
        }

        return true;
    }

}
