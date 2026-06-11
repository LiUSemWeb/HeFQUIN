package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public interface CostModel
{
	CompletableFuture<Double> initiateCostEstimation( PhysicalPlan p,
	                                                  QueryProcContext ctx )
			throws CostEstimationException;

}
