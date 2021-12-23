package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

public class EquilibriumConditionByRelativeSubplanCount implements EquilibriumConditionForSimulatedAnnealing {
	
    protected final int scalingFactor;

    public EquilibriumConditionByRelativeSubplanCount( final int scalingFactor ) {
        this.scalingFactor = scalingFactor;
    }

	public boolean isEquilibrium(final int currentGeneration, final int subplans) {
		return (currentGeneration >= subplans * scalingFactor);
	}

}
