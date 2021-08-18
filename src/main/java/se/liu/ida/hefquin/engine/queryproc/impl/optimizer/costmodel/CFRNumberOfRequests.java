package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CardinalityEstimation;

public class CFRNumberOfRequests extends CFRBase
{
	public CFRNumberOfRequests( final CardinalityEstimation cardEstimate ) {
		super(cardEstimate);
	}

	@Override
	public CompletableFuture<Integer> initiateCostEstimation( final PhysicalPlan plan ) {
		final PhysicalOperator rootOp = plan.getRootOperator();

		if ( rootOp instanceof PhysicalOpIndexNestedLoopsJoin ) {
			return initiateCardinalityEstimation( plan.getSubPlan(0) );
		}

		final int result;
		if (    rootOp instanceof PhysicalOpBindJoin
		     || rootOp instanceof PhysicalOpBindJoinWithUNION
		     || rootOp instanceof PhysicalOpBindJoinWithFILTER
		     || rootOp instanceof PhysicalOpBindJoinWithVALUES ) {
			// TODO: Returning 1 is not entirely correct here. The actual number of requests depends on the page size used for the bind-join requests.
			result = 1;
		}
		else if ( rootOp instanceof PhysicalOpRequest ) {
			// TODO: Returning 1 is not entirely correct here. The actual number of requests depends on the page size used for the requests.
			result = 1;
		}
		else if ( rootOp instanceof BasePhysicalOpBinaryJoin ) {
			result = 0;
		}
		else {
			throw createIllegalArgumentException(rootOp);
		}

		return CompletableFuture.completedFuture(result);
	}

}
