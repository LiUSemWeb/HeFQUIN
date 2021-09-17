package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

public interface TerminationCriterion {
    /**
     * Returns true if ...
     */
     boolean readyToTerminate(int generationNumber,
                                    List<PhysicalPlanWithCost> currentGeneration,
                                    List<List<PhysicalPlanWithCost>> previousGenerations);

}
