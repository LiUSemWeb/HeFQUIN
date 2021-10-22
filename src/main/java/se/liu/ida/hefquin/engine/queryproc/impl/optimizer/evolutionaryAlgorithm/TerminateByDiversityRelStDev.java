package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

/**
 * Diversity-based termination criterion:
 *
 * Termination is triggered when the relative standard deviation of the cost values
 * within the current generation is below a given threshold or the N-th generation is reached.
 */
public class TerminateByDiversityRelStDev implements TerminationCriterion
{
    protected final double relStDevThreshold;
    protected final int nrGenerations;

    public TerminateByDiversityRelStDev( final double relStDevThreshold, final int nrGenerations ) {
        this.relStDevThreshold = relStDevThreshold;
        this.nrGenerations = nrGenerations;
    }


    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        if ( allPreviousGenerations.size() + 1 < nrGenerations ) {
            final double deviation = PhysicalPlanWithCostUtils.calculateStDevCostOfPlans( currentGeneration.plans, currentGeneration.avgCost );
            final double relStDeviation = deviation / currentGeneration.avgCost;

            return relStDeviation < relStDevThreshold;
        }
        return true;
    }

}
