package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

/**
 * Termination is triggered when the lowest-cost plan per generation
 * has not exceeded a specified threshold for a number of generations.
 */

public class TerminatedByCostValue implements TerminationCriterion{
    protected final double costValueThreshold;
    protected final int nrGenerations;

    public TerminatedByCostValue( final double costValueThreshold, final int nrGenerations ) {
        this.costValueThreshold = costValueThreshold;
        this.nrGenerations = nrGenerations;
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        final int previousGenerationNr = allPreviousGenerations.size();
        if ( previousGenerationNr + 1 < nrGenerations ) {
            return false;
        }

        double bestPlanCost = currentGeneration.bestPlan.getWeight();
        int nrGensForSteadyState = 0;
        while ( nrGensForSteadyState < nrGenerations ) {
            if ( bestPlanCost > costValueThreshold ) {
                return false;
            }

            bestPlanCost = allPreviousGenerations.get( previousGenerationNr-nrGensForSteadyState ).bestPlan.getWeight();
            nrGensForSteadyState++;
        }

        return true;
    }

}
