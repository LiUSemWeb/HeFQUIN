package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;

public interface QueryOptimizerFactory
{
	QueryOptimizer createQueryOptimizer( QueryOptimizationContext ctxt );
}
