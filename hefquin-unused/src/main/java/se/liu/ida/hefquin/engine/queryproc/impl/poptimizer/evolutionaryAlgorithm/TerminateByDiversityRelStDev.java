package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCostUtils;

import java.util.List;

/**
 * Diversity-based termination criterion:
 *
 * Termination is triggered when the relative standard deviation of the cost values
 * within the current generation is below a given threshold or the N-th generation is reached.
 */
public class TerminateByDiversityRelStDev extends TerminationCriterionBase
{
	public static TerminationCriterionFactory getFactory( final double relStDevThreshold ) {
		return new TerminationCriterionFactory() {
			@Override public TerminationCriterion createInstance( final LogicalPlan plan ) {
				return new TerminateByDiversityRelStDev(relStDevThreshold, plan);
			}
		};
	}


    protected final double relStDevThreshold;

    public TerminateByDiversityRelStDev( final double relStDevThreshold, final LogicalPlan plan ) {
        super(plan);
        this.relStDevThreshold = relStDevThreshold;
    }

    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> allPreviousGenerations ) {
        if ( allPreviousGenerations.size() + 1 < nrGenerations ) {
            final double deviation = PhysicalPlanWithCostUtils.calculateStDevCostOfPlans( currentGeneration.plans, currentGeneration.avgCost );
            final double relStDeviation = deviation / currentGeneration.avgCost;

            return relStDeviation < relStDevThreshold;
        }
        return true;
    }

}
