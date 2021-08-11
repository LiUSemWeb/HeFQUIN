package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;

public interface CostModel
{
	double estimateCost( PhysicalPlan p ) throws QueryOptimizationException;
}
