package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

import java.util.List;
/**
 * Diversity-based termination criterion:
 *
 * Termination is triggered when the relative difference between the cost of the best plan
 * and the worst plan within each generation has not exceeded
 * a specified distance threshold for a number of generations.
 */
public class TerminateByDiversityDistMax implements TerminationCriterion
{
    protected final double distMaxThreshold;
    protected int nrGenerations;

    public TerminateByDiversityDistMax( final double distMaxThreshold ) {
        this.distMaxThreshold = distMaxThreshold;
    }

    @Override
    public void initialize( final LogicalPlan plan ){
        this.nrGenerations = InitializeNrGeneration.countNumOfOp(plan);
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        final int previousGenerationNr = allPreviousGenerations.size();
        if ( previousGenerationNr < nrGenerations ) {
            return false;
        }

        double bestPlanCost = currentGeneration.bestPlan.getWeight();
        double worstPlanCost = currentGeneration.worstPlan.getWeight();

        int nrGensForSteadyState = 0;
        while ( nrGensForSteadyState < nrGenerations ) {
            final double relMaxDifference = ( worstPlanCost - bestPlanCost ) / worstPlanCost;
            if ( relMaxDifference > distMaxThreshold ) {
                return false;
            }

            nrGensForSteadyState++;

            final Generation previousGen = allPreviousGenerations.get( previousGenerationNr-nrGensForSteadyState );
            bestPlanCost = previousGen.bestPlan.getWeight();
            worstPlanCost = previousGen.worstPlan.getWeight();
        }

        return true;
    }

}
