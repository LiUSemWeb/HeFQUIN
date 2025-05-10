package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import java.util.concurrent.CompletableFuture;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpBindJoinWithRequestOps;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;

public class CFRNumberOfRequests extends CFRBase
{
	public static final int defaultPageSize = 100;
	public CFRNumberOfRequests( final CardinalityEstimation cardEstimate ) {
		super(cardEstimate);
	}

	@Override
	public CompletableFuture<Integer> initiateCostEstimation( final PhysicalPlan plan ) {
		final PhysicalOperator rootOp = plan.getRootOperator();

		final double pageSize = defaultPageSize;
		final double blockSize = BaseForExecOpBindJoinWithRequestOps.DEFAULT_BATCH_SIZE;
		final CompletableFuture<Integer> numReq;

		if ( rootOp instanceof PhysicalOpIndexNestedLoopsJoin ) {
			LogicalOperator lop = (((PhysicalOpIndexNestedLoopsJoin) rootOp).getLogicalOperator());
			if ( lop instanceof LogicalOpBGPAdd) {
				FederationMember fm = ((LogicalOpBGPAdd)((PhysicalOpIndexNestedLoopsJoin) rootOp).getLogicalOperator()).getFederationMember();
				if (fm instanceof SPARQLEndpoint) {
					return initiateCardinalityEstimation(plan.getSubPlan(0));
				}
				else
					throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
			}
			else if ( lop instanceof LogicalOpGPAdd) {
				FederationMember fm = ((LogicalOpGPAdd)((PhysicalOpIndexNestedLoopsJoin) rootOp).getLogicalOperator()).getFederationMember();
				if (fm instanceof SPARQLEndpoint) {
					return initiateCardinalityEstimation(plan.getSubPlan(0));
				}
				else
					throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
			}
			else if ( lop instanceof LogicalOpTPAdd) {
				FederationMember fm = ((LogicalOpTPAdd)((PhysicalOpIndexNestedLoopsJoin) rootOp).getLogicalOperator()).getFederationMember();
				if (fm instanceof SPARQLEndpoint) {
					return initiateCardinalityEstimation( plan.getSubPlan(0) );
				}
				else if ((fm instanceof TPFServer) || (fm instanceof BRTPFServer)) {
//				The actual number of requests depends on the page size of response.
//				This implementation is under an assumption that the number of pages is evenly distributed among bind-join requests.
					numReq = initiateCardinalityEstimation(plan);
					return numReq.thenCombine(initiateCardinalityEstimation(plan.getSubPlan(0)),
							(responseSize, card) -> {
								final double pageNum = Math.ceil( responseSize/pageSize );
								final int avgPageNum = (int) Math.ceil( pageNum / card );
								return avgPageNum * card;
					});
				}
				else
					throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
			}
			else
				throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
		}
		else if ( rootOp instanceof PhysicalOpBindJoin ){
//			The actual number of requests depends on the block size used for the bind-join requests, and page size of response.
//		    This implementation is under an assumption that the number of pages is evenly distributed among bind-join requests.
			numReq = initiateCardinalityEstimation(plan);
			return numReq.thenCombine(
					initiateCardinalityEstimation(plan.getSubPlan(0)),
					(responseSize, card) -> {
						final double pageNum = Math.ceil( responseSize/pageSize );
						final int opeNum = (int) Math.ceil( card/blockSize );
						final int avgPageNum = (int) Math.ceil( pageNum / opeNum );
						return avgPageNum * opeNum;
			} );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithUNION
		     || rootOp instanceof PhysicalOpBindJoinWithFILTER
		     || rootOp instanceof PhysicalOpBindJoinWithVALUES ) {
//			The actual number of requests depends on the block size used for the bind-join requests.
			numReq = initiateCardinalityEstimation( plan.getSubPlan(0) );
			return numReq.thenApply( card -> (int) Math.ceil(card/blockSize) );
		}
		else if ( rootOp instanceof PhysicalOpRequest ) {
			FederationMember fm = ((PhysicalOpRequest<?, ?>) rootOp).getLogicalOperator().getFederationMember();
			if ( fm instanceof SPARQLEndpoint ){
				return CompletableFuture.completedFuture(1);
			}
			else if ( (fm instanceof TPFServer) || (fm instanceof BRTPFServer) ){
//				The actual number of requests depends on the page size used for the requests.
				numReq = initiateCardinalityEstimation(plan);
				return numReq.thenApply( card -> (int) Math.ceil(card/pageSize) );
			}
			else
				throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
		}
		else if ( rootOp instanceof BaseForPhysicalOpBinaryJoin
				|| rootOp instanceof PhysicalOpBinaryUnion
				|| rootOp instanceof PhysicalOpMultiwayUnion
				|| rootOp instanceof PhysicalOpLocalToGlobal
				|| rootOp instanceof PhysicalOpGlobalToLocal ) {
			return CompletableFuture.completedFuture(0);
		}
		else {
			throw createIllegalArgumentException(rootOp);
		}
	}

}
