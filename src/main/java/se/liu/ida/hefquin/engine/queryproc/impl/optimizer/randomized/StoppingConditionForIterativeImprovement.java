package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

public interface StoppingConditionForIterativeImprovement {
	public boolean readyToStop(final int currentGeneration);
}
