package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface CostEstimateProcessor
{
	void process( double estimatedCost, PhysicalPlan plan ) throws CostEstimateProcessingException;
}
