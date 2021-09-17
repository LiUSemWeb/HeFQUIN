package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;
/**
    termination criterion: number of generations
 **/
public class TerminatedByNumberOfGenerations implements TerminationCriterion{

    @Override
    public boolean readyToTerminate( final int generationNumber, final List<PhysicalPlanWithCost> currentGeneration, final List<List<PhysicalPlanWithCost>> previousGenerations ) {

        return ( previousGenerations.size() >= generationNumber-1 );

    }

}
