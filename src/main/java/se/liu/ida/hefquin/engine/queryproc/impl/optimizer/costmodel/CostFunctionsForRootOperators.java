package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface CostFunctionsForRootOperators
{
	CompletableFuture<Integer> determineNumberOfRequests( final PhysicalPlan pp );

	CompletableFuture<Integer> determineShippedRDFTermsForRequests( final PhysicalPlan pp );

	CompletableFuture<Integer> determineShippedVarsForRequests( final PhysicalPlan pp );

	CompletableFuture<Integer> determineShippedRDFTermsForResponses( final PhysicalPlan pp );

	CompletableFuture<Integer> determineShippedVarsForResponses( final PhysicalPlan pp );

	CompletableFuture<Integer> determineIntermediateResultsSize( final PhysicalPlan pp );
}
