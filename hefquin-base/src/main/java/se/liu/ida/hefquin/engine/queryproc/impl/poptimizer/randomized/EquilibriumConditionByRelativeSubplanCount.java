package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized;

public class EquilibriumConditionByRelativeSubplanCount implements EquilibriumConditionForSimulatedAnnealing
{
	public final static int DEFAULT_SCALING_FACTOR = 16;

	protected final int scalingFactor;

	public EquilibriumConditionByRelativeSubplanCount( final int scalingFactor ) {
		this.scalingFactor = scalingFactor;
	}

	public EquilibriumConditionByRelativeSubplanCount() {
		this(DEFAULT_SCALING_FACTOR);
	}

	@Override
	public boolean isEquilibrium( final int currentGeneration, final int subplans ) {
		return (currentGeneration >= subplans * scalingFactor);
	}

}
