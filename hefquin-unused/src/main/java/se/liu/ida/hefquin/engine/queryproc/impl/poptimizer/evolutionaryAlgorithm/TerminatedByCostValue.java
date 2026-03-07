package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

/**
 * Termination is triggered when the lowest-cost plan per generation
 * has not exceeded a specified threshold for a number of generations.
 */
public class TerminatedByCostValue extends TerminationCriterionBase
{
	public static TerminationCriterionFactory getFactory( final double costValueThreshold ) {
		return new TerminationCriterionFactory() {
			@Override public TerminationCriterion createInstance( final LogicalPlan plan ) {
				return new TerminatedByCostValue(costValueThreshold, plan);
			}
		};
	}


    protected final double costValueThreshold;

    public TerminatedByCostValue( final double costValueThreshold, final LogicalPlan plan ) {
        super(plan);
        this.costValueThreshold = costValueThreshold;
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
