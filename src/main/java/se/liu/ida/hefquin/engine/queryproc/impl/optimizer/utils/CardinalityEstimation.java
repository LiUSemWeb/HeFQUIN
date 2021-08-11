package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface CardinalityEstimation
{
	int getCardinalityEstimationOfLeafNode( PhysicalPlan pp ) throws CardinalityEstimationException;

	int getJoinCardinalityEstimation( PhysicalPlan pp ) throws CardinalityEstimationException;

	int getTPAddCardinalityEstimation( PhysicalPlan pp ) throws CardinalityEstimationException;

	int getBGPAddCardinalityEstimation( PhysicalPlan pp ) throws CardinalityEstimationException;

}
