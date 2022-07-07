package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CardinalityEstimation;

public class CFRNumberOfTermsShippedInResponses extends CFRBase
{
	public CFRNumberOfTermsShippedInResponses( final CardinalityEstimation cardEstimate ) {
		super(cardEstimate);
	}

	@Override
	public CompletableFuture<Integer> initiateCostEstimation( final PhysicalPlan plan ) {
		final PhysicalOperator pop = plan.getRootOperator();
		final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) pop).getLogicalOperator();

		if ( lop instanceof LogicalOpTPAdd ) {
			final CompletableFuture<Integer> futureIntResSize = initiateCardinalityEstimation(plan);
			final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
			final FederationMember fm = tpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint ) {
				final int numberOfVars = tpAdd.getTP().numberOfVars();
				return futureIntResSize.thenApply( intResSize -> numberOfVars * intResSize );
			}
			else if ( fm instanceof TPFServer || fm instanceof BRTPFServer) {
				return futureIntResSize.thenApply( intResSize -> 3 * intResSize );
			}
			else {
				futureIntResSize.cancel(true);
				throw createIllegalArgumentException(fm);
			}
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			final CompletableFuture<Integer> futureIntResSize = initiateCardinalityEstimation(plan);
			final LogicalOpBGPAdd bgpAdd = (LogicalOpBGPAdd) lop;
			final FederationMember fm = bgpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint ) {
				final int numberOfVars = QueryPatternUtils.getVariablesInPattern( bgpAdd.getBGP() ).size();
				return futureIntResSize.thenApply( intResSize -> numberOfVars * intResSize );
			}
			else {
				futureIntResSize.cancel(true);
				throw createIllegalArgumentException(fm);
			}
		}
		else if ( lop instanceof LogicalOpRequest ) {
			final CompletableFuture<Integer> futureIntResSize = initiateCardinalityEstimation(plan);
			final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();

			if ( fm instanceof SPARQLEndpoint ) {
				final SPARQLRequest req = (SPARQLRequest) ((LogicalOpRequest<?, ?>) lop).getRequest();
				final int numberOfVars = QueryPatternUtils.getVariablesInPattern( req.getQueryPattern() ).size();
				return futureIntResSize.thenApply( intResSize -> numberOfVars * intResSize );
			}
			else if ( fm instanceof TPFServer || fm instanceof BRTPFServer) {
				return futureIntResSize.thenApply( intResSize -> 3 * intResSize );
			}
			else {
				futureIntResSize.cancel(true);
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
