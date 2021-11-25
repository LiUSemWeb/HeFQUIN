package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

public class StoppingConditionByNumberOfGenerations implements StoppingConditionForIterativeImprovement{
	
    protected final int generationThreshold;

    public StoppingConditionByNumberOfGenerations( final int generationThreshold ) {
        this.generationThreshold = generationThreshold;
    }

	public boolean readyToStop(final int currentGeneration) {

		// As the stopping condition is for the iterative improvement algorithm, we know that it it is checked once per loop.
		// We can thus increment the current number of generations every time this is called.
		return (currentGeneration > generationThreshold);
	}
}
