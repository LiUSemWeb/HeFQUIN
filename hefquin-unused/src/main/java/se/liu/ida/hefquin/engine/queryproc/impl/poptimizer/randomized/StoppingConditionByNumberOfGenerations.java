package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized;

public class StoppingConditionByNumberOfGenerations implements StoppingConditionForIterativeImprovement
{
	protected final int generationThreshold;

	public StoppingConditionByNumberOfGenerations( final int generationThreshold ) {
		this.generationThreshold = generationThreshold;
	}

	public boolean readyToStop( final int currentGeneration ) {
		return (currentGeneration > generationThreshold);
	}

}
