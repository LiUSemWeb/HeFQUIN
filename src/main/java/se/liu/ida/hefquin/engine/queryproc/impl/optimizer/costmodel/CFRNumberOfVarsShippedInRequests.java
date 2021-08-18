package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import java.util.concurrent.CompletableFuture;

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
import se.liu.ida.hefquin.engine.queryplan.physical.impl.BasePhysicalOpBinaryJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithFILTER;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithVALUES;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpIndexNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.cardinality.CardinalityEstimationHelper;

public class CFRNumberOfVarsShippedInRequests extends CFRBase
{
	public CFRNumberOfVarsShippedInRequests( final CardinalityEstimation cardEstimate ) {
		super(cardEstimate);
	}

	@Override
	public CompletableFuture<Integer> initiateCostEstimation( final PhysicalPlan plan ) {
		final PhysicalOperator pop = plan.getRootOperator();
		final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) pop).getLogicalOperator();

		final int numberOfVars;
		final int numberOfJoinVars;
		final CompletableFuture<Integer> futureIntResSize;

		if ( lop instanceof LogicalOpTPAdd ) {
			final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
			numberOfVars = QueryPatternUtils.getNumberOfVarOccurrences( tpAdd.getTP() );

			final PhysicalPlan subplan = plan.getSubPlan(0);
			final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestPlan(tpAdd);
			numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(subplan,reqTP).size();

			futureIntResSize = initiateCardinalityEstimation(subplan);
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			final LogicalOpBGPAdd bgpAdd = (LogicalOpBGPAdd) lop;
			numberOfVars = QueryPatternUtils.getNumberOfVarOccurrences( bgpAdd.getBGP() );

			final PhysicalPlan subplan = plan.getSubPlan(0);
			final PhysicalPlan reqBGP = CardinalityEstimationHelper.formRequestPlan(bgpAdd);
			numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(subplan,reqBGP).size();

			if ( pop instanceof PhysicalOpBindJoinWithVALUES ) {
				futureIntResSize = null; // irrelevant
			} else {
				futureIntResSize = initiateCardinalityEstimation(subplan);
			}
		}
		else if ( lop instanceof LogicalOpRequest ) {
			final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();
			if ( req instanceof TriplePatternRequest ) {
				final TriplePatternRequest tpReq = (TriplePatternRequest) req;
				numberOfVars = tpReq.getQueryPattern().numberOfVars();
			}
			else if ( req instanceof SPARQLRequest ) {
				final SPARQLRequest sparqlReq = (SPARQLRequest) req;
				// TODO: The following line is not correct. Not every SPARQLRequest contains a BGP.
				numberOfVars = QueryPatternUtils.getNumberOfVarOccurrences((BGP) sparqlReq.getQueryPattern());
			}
			else {
				throw createIllegalArgumentException(req);
			}
			numberOfJoinVars = 0;    // irrelevant for request operators
			futureIntResSize = null; // irrelevant for request operators
		}
		else if ( lop instanceof LogicalOpJoin ) {
			numberOfVars = 0;        // irrelevant for join operators
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
			return futureIntResSize.thenApply( intResSize -> intResSize * (numberOfVars - numberOfJoinVars) );
		}
		else if (    pop instanceof PhysicalOpBindJoinWithFILTER
		          || pop instanceof PhysicalOpBindJoin ) {
			return futureIntResSize.thenApply( intResSize -> numberOfVars + intResSize * numberOfJoinVars );
		}

		// cases in which the cost value can be calculated directly
		final int costValue;
		if ( pop instanceof PhysicalOpBindJoinWithVALUES ) {
			costValue = numberOfVars + numberOfJoinVars;
		}
		else if ( pop instanceof PhysicalOpRequest ) {
			costValue = numberOfVars;
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
