package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.cardinality;

import java.util.concurrent.CompletableFuture;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public interface VarSpecificCardinalityEstimation
{
	/**
	 * Asynchronous method that initiates a process to estimate the
	 * the variable-specific cardinality of the result that will be
	 * produced by the given plan. The actual estimate can then be
	 * obtained by using the <code>get()</code> method of the
	 * returned CompletableFuture.
	 */
	CompletableFuture<Integer> initiateCardinalityEstimation( PhysicalPlan plan, Var v );
}
