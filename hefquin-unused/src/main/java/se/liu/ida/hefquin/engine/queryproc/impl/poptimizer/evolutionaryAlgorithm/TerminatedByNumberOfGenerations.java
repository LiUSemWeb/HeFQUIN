package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
/**
 *    termination criterion: number of generations
 */
public class TerminatedByNumberOfGenerations extends TerminationCriterionBase
{
	public static TerminationCriterionFactory getFactory( final int generationThreshold ) {
		return new TerminationCriterionFactory() {
			@Override public TerminationCriterion createInstance( final LogicalPlan plan ) {
				return new TerminatedByNumberOfGenerations(generationThreshold, plan);
			}
		};
	}


    protected final int generationThreshold;

    public TerminatedByNumberOfGenerations( final int generationThreshold, final LogicalPlan plan ) {
        super(plan);
        this.generationThreshold = generationThreshold;
    }

    /**
     * Returns true if the generation number exceeds the threshold
     */
    @Override
    public boolean readyToTerminate( final Generation currentGeneration, final List<Generation> previousGenerations) {
        return ( previousGenerations.size() + 1 >= generationThreshold );
    }

}
