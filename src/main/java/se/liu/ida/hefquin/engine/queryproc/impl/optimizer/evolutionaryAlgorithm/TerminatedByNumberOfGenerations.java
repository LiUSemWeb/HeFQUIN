package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

import java.util.List;
/**
 *    termination criterion: number of generations
 */
public class TerminatedByNumberOfGenerations implements TerminationCriterion{

    protected final int generationThreshold;

    public TerminatedByNumberOfGenerations( final int generationThreshold ) {
        this.generationThreshold = generationThreshold;
    }

    @Override
    public void initialize( final LogicalPlan plan ){
//       Do nothing
    }

    /**
     * Returns true if the generation number exceeds the threshold
     */
    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> previousGenerations) {
        return ( previousGenerations.size() + 1 >= generationThreshold );
    }

}
