package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

public interface EquilibriumConditionForSimulatedAnnealing {
	public boolean isEquilibrium(final int currentGeneration, final int subplanCount);
}
