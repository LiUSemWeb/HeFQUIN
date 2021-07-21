package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public interface CardinalityEstimation
{
	int getCardinalityEstimationOfLeafNode( PhysicalPlan pp ) throws QueryOptimizationException;

	int getJoinCardinalityEstimation( PhysicalPlan pp ) throws QueryOptimizationException;

	int getTPAddCardinalityEstimation( PhysicalPlan pp ) throws QueryOptimizationException;

	int getBGPAddCardinalityEstimation( PhysicalPlan pp ) throws QueryOptimizationException;

}
