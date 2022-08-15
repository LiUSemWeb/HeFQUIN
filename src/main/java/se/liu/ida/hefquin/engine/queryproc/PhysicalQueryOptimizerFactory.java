package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.QueryOptimizationContext;

public interface PhysicalQueryOptimizerFactory
{
	PhysicalOptimizer createQueryOptimizer( QueryOptimizationContext ctxt );
}
