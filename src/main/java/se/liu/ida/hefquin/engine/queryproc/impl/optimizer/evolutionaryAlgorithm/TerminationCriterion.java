package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

import java.util.List;

public interface TerminationCriterion {
    /**
     * Returns true if this termination criterion has been reached with the current generation.
     *
     * @param currentGeneration the current generation for which the termination criterion is test
     * @param allPreviousGenerations a list of all previous generations, in the order in which they have been created
     */
    boolean readyToTerminate( final Generation currentGeneration,
                              final List<Generation> allPreviousGenerations );

    void initialize( final LogicalPlan plan );

}
