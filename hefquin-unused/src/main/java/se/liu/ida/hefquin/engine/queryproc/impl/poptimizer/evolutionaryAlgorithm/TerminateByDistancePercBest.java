package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

/**
 * Distance-based termination criterion:
 *
 * Termination is triggered when the relative distance between the cost of the best plan
 * in the current generation and in the previous generation has not exceeded
 * a specified distance threshold for a number of generations.
 */
public class TerminateByDistancePercBest extends TerminationCriterionBase
{
	public static TerminationCriterionFactory getFactory( final double percBestThreshold ) {
		return new TerminationCriterionFactory() {
			@Override public TerminationCriterion createInstance( final LogicalPlan plan ) {
				return new TerminateByDistancePercBest(percBestThreshold, plan);
			}
		};
	}


    protected final double percBestThreshold;

    public TerminateByDistancePercBest( final double percBestThreshold, final LogicalPlan plan ) {
        super(plan);
        this.percBestThreshold = percBestThreshold;
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        final int previousGenerationNr = allPreviousGenerations.size();
        if ( previousGenerationNr < nrGenerations ) {
            return false;
        }

        double bestCurrentCost = currentGeneration.bestPlan.getWeight();
        double bestPreviousCost;
        double relDistance;

        int nrGensForSteadyState = 0;
        while ( nrGensForSteadyState < nrGenerations ) {
            bestPreviousCost = allPreviousGenerations.get( previousGenerationNr-nrGensForSteadyState-1 ).bestPlan.getWeight();
            relDistance = ( bestPreviousCost - bestCurrentCost ) / bestPreviousCost;
            if ( relDistance > percBestThreshold ) {
                return false;
            }

            bestCurrentCost = bestPreviousCost;
            nrGensForSteadyState++;
        }

        return true;
    }

}
