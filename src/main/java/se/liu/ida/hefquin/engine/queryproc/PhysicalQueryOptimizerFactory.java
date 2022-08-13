package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;

public interface PhysicalQueryOptimizerFactory
{
	PhysicalQueryOptimizer createQueryOptimizer( QueryOptimizationContext ctxt );
}
