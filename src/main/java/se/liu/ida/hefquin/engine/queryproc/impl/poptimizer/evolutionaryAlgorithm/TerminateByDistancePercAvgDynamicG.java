package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

/**
 * Distance-based termination criterion:
 *
 * Termination is triggered when the relative distance between the average cost
 * in the current generation and in the previous generation has not exceeded
 * a specified distance threshold for a number of generations.
 * (The nrGenerations is a dynamic number depending on the number of lowest-cost plans).
 */
public class TerminateByDistancePercAvgDynamicG extends TerminationCriterionBase
{
	public static TerminationCriterionFactory getFactory( final double percAvgThreshold ) {
		return new TerminationCriterionFactory() {
			@Override public TerminationCriterion createInstance( final LogicalPlan plan ) {
				return new TerminateByDistancePercAvgDynamicG(percAvgThreshold, plan);
			}
		};
	}


    protected final double percAvgThreshold;

    public TerminateByDistancePercAvgDynamicG( final double percAvgThreshold, final LogicalPlan plan ) {
        super(plan);
        this.percAvgThreshold = percAvgThreshold;
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        final int previousGenerationNr = allPreviousGenerations.size();
        if ( previousGenerationNr < 1 ) {
            return false;
        }

//      Update nrGenerations
        double percentageNrOfPlansWithBestCost = currentGeneration.nrOfPlansWithBestCost / (double) allPreviousGenerations.get( previousGenerationNr-1 ).nrOfPlansWithBestCost;
        nrGenerations = (int) Math.ceil( nrGenerations / percentageNrOfPlansWithBestCost);
        if ( previousGenerationNr < nrGenerations ) {
            return false;
        }

        double avgCurrentCost = currentGeneration.avgCost;
        double avgPreviousCost;
        double relDistance;
        Generation previousGen;

        int nrGensForSteadyState = 0;
        while ( nrGensForSteadyState < nrGenerations ) {
            previousGen = allPreviousGenerations.get( previousGenerationNr-nrGensForSteadyState-1 );
            avgPreviousCost = previousGen.avgCost;
            relDistance = ( avgPreviousCost - avgCurrentCost ) / avgPreviousCost;
            if ( relDistance > percAvgThreshold ) {
                return false;
            }

            avgCurrentCost = avgPreviousCost;
            nrGensForSteadyState ++;
        }

        return true;
    }

}
