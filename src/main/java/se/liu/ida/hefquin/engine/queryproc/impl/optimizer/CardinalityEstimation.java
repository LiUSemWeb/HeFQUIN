package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimationUtils;

/**
 * This interface represents cardinality estimators that can estimate
 * the cardinality of the result that will be produced by query plans.
 * Different implementations of this interface may apply different
 * approaches to estimate the cardinality.
 */
public interface CardinalityEstimation
{
	/**
	 * Asynchronous method that initiates a process to estimate the
	 * cardinality of the result that will be produced by the given
	 * plan. The actual estimate can then be obtained by using the
	 * <code>get()</code> method of the returned CompletableFuture.
	 * 
	 * For some helper methods that wrap calls to this method and
	 * provide its functionality as synchronous functions, refer to
	 * {@link CardinalityEstimationUtils}.
	 */
	CompletableFuture<Integer> initiateCardinalityEstimation( PhysicalPlan plan );
}
