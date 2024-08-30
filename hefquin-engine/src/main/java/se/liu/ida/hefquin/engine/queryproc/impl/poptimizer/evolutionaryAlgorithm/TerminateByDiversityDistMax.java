package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
/**
 * Diversity-based termination criterion:
 *
 * Termination is triggered when the relative difference between the cost of the best plan
 * and the worst plan within each generation has not exceeded
 * a specified distance threshold for a number of generations.
 */
public class TerminateByDiversityDistMax extends TerminationCriterionBase
{
	public static TerminationCriterionFactory getFactory( final double distMaxThreshold ) {
		return new TerminationCriterionFactory() {
			@Override public TerminationCriterion createInstance( final LogicalPlan plan ) {
				return new TerminateByDiversityDistMax(distMaxThreshold, plan);
			}
		};
	}


    protected final double distMaxThreshold;

    public TerminateByDiversityDistMax( final double distMaxThreshold, final LogicalPlan plan ) {
        super(plan);
        this.distMaxThreshold = distMaxThreshold;
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
