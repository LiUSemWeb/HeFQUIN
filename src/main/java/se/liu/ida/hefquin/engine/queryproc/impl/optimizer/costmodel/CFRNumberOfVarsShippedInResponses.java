package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
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
			final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();

			// TODO: Check whether the the following is correct. I believe that
			// this part should be defined in terms of the type of federation
			// member and it should be similar to the case of LogicalOpTPAdd.

			if ( req instanceof TriplePatternRequest ) {
				return CompletableFuture.completedFuture(0);
			}
			else if ( req instanceof SPARQLRequest ) {
				final SPARQLRequest sparqlReq = (SPARQLRequest) req;
				// TODO: The following line is not correct. Not every SPARQLRequest contains a BGP.
				final int numberOfVars = QueryPatternUtils.getVariablesInPattern( (BGP) sparqlReq.getQueryPattern() ).size();
				final CompletableFuture<Integer> futureIntResSize = initiateCardinalityEstimation(plan);
				return futureIntResSize.thenApply( intResSize -> numberOfVars * intResSize );
			}
			else {
				throw createIllegalArgumentException(req);
			}
		}
		else if ( lop instanceof LogicalOpJoin ) {
			return CompletableFuture.completedFuture(0);
		}
		else {
			throw createIllegalArgumentException(lop);
		}
	}

}
