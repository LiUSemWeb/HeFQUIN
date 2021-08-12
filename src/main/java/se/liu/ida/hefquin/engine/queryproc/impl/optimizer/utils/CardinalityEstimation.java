package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface CardinalityEstimation
{
	int getCardinalityEstimation( PhysicalPlan plan ) throws CardinalityEstimationException;
}
