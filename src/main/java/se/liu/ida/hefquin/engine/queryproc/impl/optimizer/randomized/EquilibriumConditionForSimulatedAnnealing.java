package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

public interface EquilibriumConditionForSimulatedAnnealing
{
	boolean isEquilibrium(int currentGeneration, int subplanCount);
}
