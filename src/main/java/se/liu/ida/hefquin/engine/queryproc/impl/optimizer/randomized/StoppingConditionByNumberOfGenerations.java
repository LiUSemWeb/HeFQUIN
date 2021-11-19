package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

public class StoppingConditionByNumberOfGenerations implements StoppingConditionForIterativeImprovement{
	
    protected final int generationThreshold;
    protected int generations;

    public StoppingConditionByNumberOfGenerations( final int generationThreshold ) {
        this.generationThreshold = generationThreshold;
        this.generations = 0;
    }

	
	@Override
	public boolean readyToStop() {

		// As the stopping condition is for the iterative improvement algorithm, we know that it it is checked once per loop.
		// We can thus increment the current number of generations every time this is called.
		generations += 1;
		return (generations > generationThreshold);
	}
}
