package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CardinalityEstimation;

public class CFRNumberOfVarsShippedInResponses extends CFRBase
{
	public CFRNumberOfVarsShippedInResponses( final CardinalityEstimation cardEstimate ) {
		super(cardEstimate);
	}

	@Override
	public CompletableFuture<Integer> initiateCostEstimation( final PhysicalPlan plan ) {
		final PhysicalOperator pop = plan.getRootOperator();
		final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) pop).getLogicalOperator();

		if ( lop instanceof LogicalOpTPAdd ) {
			final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
			final FederationMember fm = tpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint ) {
				final int numberOfVars = tpAdd.getTP().numberOfVars();
				final CompletableFuture<Integer> futureIntResSize = initiateCardinalityEstimation(plan);
				return futureIntResSize.thenApply( intResSize -> numberOfVars * intResSize );
			}
			else if ( fm instanceof TPFServer || fm instanceof BRTPFServer) {
				return CompletableFuture.completedFuture(0);
			}
			else {
				throw createIllegalArgumentException(fm);
			}
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			final LogicalOpBGPAdd bgpAdd = (LogicalOpBGPAdd) lop;
			final FederationMember fm = bgpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint ) {
				final int numberOfVars = QueryPatternUtils.getVariablesInPattern( bgpAdd.getBGP() ).size();
				final CompletableFuture<Integer> futureIntResSize = initiateCardinalityEstimation(plan);
				return futureIntResSize.thenApply( intResSize -> numberOfVars * intResSize );
			}
			else {
				throw createIllegalArgumentException(fm);
			}
		}
		else if ( lop instanceof LogicalOpRequest ) {
			final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();

			if ( fm instanceof SPARQLEndpoint ) {
				final SPARQLRequest req = (SPARQLRequest) ((LogicalOpRequest<?, ?>) lop).getRequest();
				final int numberOfVars = QueryPatternUtils.getVariablesInPattern( req.getQueryPattern() ).size();
				final CompletableFuture<Integer> futureIntResSize = initiateCardinalityEstimation(plan);
				return futureIntResSize.thenApply( intResSize -> numberOfVars * intResSize );
			}
			else if ( fm instanceof TPFServer || fm instanceof BRTPFServer) {
				return CompletableFuture.completedFuture(0);
			}
			else {
				throw createIllegalArgumentException(fm);
			}
		}
		else if ( lop instanceof LogicalOpJoin || lop instanceof LogicalOpUnion ) {
			return CompletableFuture.completedFuture(0);
		}
		else {
			throw createIllegalArgumentException(lop);
		}
	}

}
