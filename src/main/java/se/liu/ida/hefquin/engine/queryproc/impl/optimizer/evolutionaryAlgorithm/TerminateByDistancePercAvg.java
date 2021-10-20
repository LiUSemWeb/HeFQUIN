package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

/**
 * Distance-based termination criterion:
 *
 * Termination is triggered when the relative distance between the average cost of plans
 * in the current generation and in the previous generation has not exceeded
 * a specified distance threshold for a number of generations.
 */

public class TerminateByDistancePercAvg implements TerminationCriterion{
    protected final double percAvgThreshold;
    protected final int nrGenerations;

    public TerminateByDistancePercAvg( final double percAvgThreshold, final int nrGenerations ) {
        this.percAvgThreshold = percAvgThreshold;
        this.nrGenerations = nrGenerations;
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        final int previousGenerationNr = allPreviousGenerations.size();
        if ( previousGenerationNr < nrGenerations ) {
            return false;
        }

        double avgCurrentCost = currentGeneration.avgCost;
        double avgPreviousCost, relDistance;

        int nrGensForSteadyState = 0;
        while ( nrGensForSteadyState < nrGenerations ) {
            avgPreviousCost = allPreviousGenerations.get( previousGenerationNr-nrGensForSteadyState-1 ).avgCost;
            relDistance = ( avgPreviousCost - avgCurrentCost ) / avgPreviousCost;
            if ( relDistance > percAvgThreshold ) {
                return false;
            }

            avgCurrentCost = avgPreviousCost;
            nrGensForSteadyState++;
        }

        return true;
    }

}
