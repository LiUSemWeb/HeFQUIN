package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

public class TerminateByDistancePercBest implements TerminationCriterion{

    protected final double percBestThreshold;
    protected final int nrGenerations;

    public TerminateByDistancePercBest( final double percBestThreshold, final int nrGenerations ) {
        this.percBestThreshold = percBestThreshold;
        this.nrGenerations = nrGenerations;
    }

    @Override
    public boolean readyToTerminate( final int generationNumber, final Generation currentGeneration, final List<Generation> previousGenerations ) {
        final int preNr = previousGenerations.size();
        if ( preNr < nrGenerations ) {
            return false;
        }

        double bestCostCur = currentGeneration.bestPlan.getWeight();
        double bestCostPre = previousGenerations.get( previousGenerations.size()-1 ).bestPlan.getWeight();
        double distance = ( bestCostPre - bestCostCur ) / bestCostPre;

        if ( distance > percBestThreshold ) {
            return false;
        }

        int nrGensForSteadyState = 1;
        while ( nrGensForSteadyState < nrGenerations ) {
            bestCostCur = bestCostPre;
            bestCostPre = previousGenerations.get( preNr-nrGensForSteadyState-1 ).bestPlan.getWeight();

            distance = ( bestCostPre - bestCostCur ) / bestCostPre;
            if ( distance <= percBestThreshold ) {
                nrGensForSteadyState++;
            }
            else {
                break;
            }
        }

        return nrGensForSteadyState == nrGenerations;
    }

}
