package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

public class CFRNumberOfVarsShippedInResponses extends CFRBase
{
	public CFRNumberOfVarsShippedInResponses( final CardinalityEstimation cardEstimate ) {
		super(cardEstimate);
	}

	@Override
	public CompletableFuture<Integer> initiateCostEstimation( final PhysicalPlan plan ) {
		final PhysicalOperator pop = plan.getRootOperator();
		final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) pop).getLogicalOperator();

		if ( lop instanceof LogicalOpGPAdd gpAdd ) {
			final FederationMember fm = gpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint ) {
				final int numberOfVars = gpAdd.getPattern().getAllMentionedVariables().size();
				final CompletableFuture<Integer> futureIntResSize = initiateCardinalityEstimation(plan);
				return futureIntResSize.thenApply( intResSize -> numberOfVars * intResSize );
			}
			else if ( fm instanceof TPFServer || fm instanceof BRTPFServer ) {
				return CompletableFuture.completedFuture(0);
			}
			else {
				throw createIllegalArgumentException(fm);
			}
		}
		else if ( lop instanceof LogicalOpRequest reqOp ) {
			final FederationMember fm = reqOp.getFederationMember();

			if ( fm instanceof SPARQLEndpoint ) {
				final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();
				final int numberOfVars = req.getQueryPattern().getAllMentionedVariables().size();
				final CompletableFuture<Integer> futureIntResSize = initiateCardinalityEstimation(plan);
				return futureIntResSize.thenApply( intResSize -> numberOfVars * intResSize );
			}
			else if ( fm instanceof TPFServer || fm instanceof BRTPFServer ) {
				return CompletableFuture.completedFuture(0);
			}
			else {
				throw createIllegalArgumentException(fm);
			}
		}
		else if (    lop instanceof LogicalOpJoin
		          || lop instanceof LogicalOpUnion
		          || lop instanceof LogicalOpMultiwayUnion
		          || lop instanceof LogicalOpLocalToGlobal
		          || lop instanceof LogicalOpGlobalToLocal ) {
			return CompletableFuture.completedFuture(0);
		}
		else {
			throw createIllegalArgumentException(lop);
		}
	}

}
