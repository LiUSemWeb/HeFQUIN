package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

/**
 * Distance-based termination criterion:
 *
 * Termination is triggered when the relative distance between the cost of the best plan
 * in the current generation and in the previous generation has not exceeded
 * a specified distance threshold for a number of generations.
 */

public class TerminateByDistancePercBest implements TerminationCriterion{

    protected final double percBestThreshold;
    protected final int nrGenerations;

    public TerminateByDistancePercBest( final double percBestThreshold, final int nrGenerations ) {
        this.percBestThreshold = percBestThreshold;
        this.nrGenerations = nrGenerations;
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> previousGenerations ) {
        final int previousGenerationNr = previousGenerations.size();
        if ( previousGenerationNr < nrGenerations ) {
            return false;
        }

        double bestCurrentCost = currentGeneration.bestPlan.getWeight();
        double bestPreviousCost = previousGenerations.get( previousGenerationNr-1 ).bestPlan.getWeight();
        double relDistance = ( bestPreviousCost - bestCurrentCost ) / bestPreviousCost;

        if ( relDistance > percBestThreshold ) {
            return false;
        }

        int nrGensForSteadyState = 1;
        while ( nrGensForSteadyState < nrGenerations ) {
            bestCurrentCost = bestPreviousCost;
            bestPreviousCost = previousGenerations.get( previousGenerationNr-nrGensForSteadyState-1 ).bestPlan.getWeight();

            relDistance = ( bestPreviousCost - bestCurrentCost ) / bestPreviousCost;
            if ( relDistance > percBestThreshold ) {
                return false;
            }

            nrGensForSteadyState++;
        }

        return true;
    }

}
