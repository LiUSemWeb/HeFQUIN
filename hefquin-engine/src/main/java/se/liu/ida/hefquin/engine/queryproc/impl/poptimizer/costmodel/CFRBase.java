package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;

/**
 * Abstract base class for implementations of {@link CostFunctionForRootOp}.
 */
public abstract class CFRBase implements CostFunctionForRootOp
{
	protected final CardinalityEstimation cardEstimate;

	public CFRBase( final CardinalityEstimation cardEstimate ) {
		assert cardEstimate != null;
		this.cardEstimate = cardEstimate;
	}

	protected CompletableFuture<Integer> initiateCardinalityEstimation( final PhysicalPlan plan ) {
    	return cardEstimate.initiateCardinalityEstimation(plan);
    }

	protected IllegalArgumentException createIllegalArgumentException( final PhysicalOperator rootOp ) {
		return new IllegalArgumentException("Physical root operator of unsupported type (" + rootOp.getClass().getName() + ").");
	}

	protected IllegalArgumentException createIllegalArgumentException( final LogicalOperator rootOp ) {
		return new IllegalArgumentException("Logical root operator of unsupported type (" + rootOp.getClass().getName() + ").");
	}

	protected IllegalArgumentException createIllegalArgumentException( final DataRetrievalRequest req ) {
		return new IllegalArgumentException("Unsupported type of request (" + req.getClass().getName() + ").");
	}

	protected IllegalArgumentException createIllegalArgumentException( final FederationMember fm ) {
		return new IllegalArgumentException("Unsupported type of federation member (" + fm.getClass().getName() + ").");
	}

}
