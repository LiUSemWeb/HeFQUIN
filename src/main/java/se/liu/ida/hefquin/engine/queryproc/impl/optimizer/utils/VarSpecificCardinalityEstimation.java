package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import java.util.concurrent.CompletableFuture;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface VarSpecificCardinalityEstimation
{
	CompletableFuture<Integer> initiateCardinalityEstimation( PhysicalPlan plan, Var v );
}
