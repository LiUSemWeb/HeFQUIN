package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized;

public interface StoppingConditionForIterativeImprovement
{
	boolean readyToStop(int currentGeneration);
}
