package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;
/**
 *    termination criterion: number of generations
 */
public class TerminatedByNumberOfGenerations implements TerminationCriterion{

    protected final int generationThreshold;

    public TerminatedByNumberOfGenerations( final int generationThreshold ) {
        this.generationThreshold = generationThreshold;
    }

    /**
     * Returns true if the generation number exceeds the threshold
     */
    @Override
    public boolean readyToTerminate( final int generationNumber, final List<PhysicalPlanWithCost> currentGeneration, final List<List<PhysicalPlanWithCost>> previousGenerations ) {
        return ( generationNumber >= generationThreshold );
    }

}
