package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.cardinality.CardinalityEstimationHelper;

public class CFRNumberOfTermsShippedInRequests extends CFRBase
{
	public CFRNumberOfTermsShippedInRequests( final CardinalityEstimation cardEstimate ) {
		super(cardEstimate);
	}

	@Override
	public CompletableFuture<Integer> initiateCostEstimation( final PhysicalPlan plan ) {
		final PhysicalOperator pop = plan.getRootOperator();
		final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) pop).getLogicalOperator();

		final int numberOfTerms;
		final int numberOfJoinVars;
		final CompletableFuture<Integer> futureIntResSize;

		if ( lop instanceof LogicalOpTPAdd ) {
			final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
			numberOfTerms = QueryPatternUtils.getNumberOfTermOccurrences( tpAdd.getTP() );

			final PhysicalPlan subplan = plan.getSubPlan(0);
			final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestPlan(tpAdd);
			numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(subplan,reqTP).size();

			futureIntResSize = initiateCardinalityEstimation(subplan);
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			final LogicalOpBGPAdd bgpAdd = (LogicalOpBGPAdd) lop;
			numberOfTerms = QueryPatternUtils.getNumberOfTermOccurrences( bgpAdd.getBGP() );

			final PhysicalPlan subplan = plan.getSubPlan(0);
			final PhysicalPlan reqBGP = CardinalityEstimationHelper.formRequestPlan(bgpAdd);
			numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(subplan,reqBGP).size();

			futureIntResSize = initiateCardinalityEstimation(subplan);
		}
		else if ( lop instanceof LogicalOpRequest ) {
			final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();
			if ( req instanceof TriplePatternRequest ) {
				final TriplePatternRequest tpReq = (TriplePatternRequest) req;
				numberOfTerms = 3 - tpReq.getQueryPattern().numberOfVars();
			}
			else if ( req instanceof SPARQLRequest ) {
				final SPARQLRequest sparqlReq = (SPARQLRequest) req;
				numberOfTerms = QueryPatternUtils.getNumberOfTermOccurrences( sparqlReq.getQueryPattern() );
			}
			else {
				throw createIllegalArgumentException(req);
			}
			numberOfJoinVars = 0;    // irrelevant for request operators
			futureIntResSize = null; // irrelevant for request operators
		}
		else if ( lop instanceof LogicalOpJoin ) {
			numberOfTerms = 0;       // irrelevant for join operators
			numberOfJoinVars = 0;    // irrelevant for join operators
			futureIntResSize = null; // irrelevant for join operators
		}
		else {
			throw createIllegalArgumentException(lop);
		}

		// cases in which the cost value depends on some intermediate
		// result size, which needs to be fetched first
		if (    pop instanceof PhysicalOpIndexNestedLoopsJoin
		     || pop instanceof PhysicalOpBindJoinWithUNION ) {
			return futureIntResSize.thenApply( intResSize -> intResSize * (numberOfTerms - numberOfJoinVars) );
		}
		else if (    pop instanceof PhysicalOpBindJoinWithFILTER
		          || pop instanceof PhysicalOpBindJoinWithVALUES
		          || pop instanceof PhysicalOpBindJoin ) {
			return futureIntResSize.thenApply( intResSize -> numberOfTerms + intResSize * numberOfJoinVars );
		}

		// cases in which the cost value can be calculated directly
		final int costValue;
		if ( pop instanceof PhysicalOpRequest ) {
			costValue = numberOfTerms;
		}
		else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
			costValue = 0;
		}
		else {
			throw createIllegalArgumentException(pop);
		}

		return CompletableFuture.completedFuture(costValue);
	}

}
