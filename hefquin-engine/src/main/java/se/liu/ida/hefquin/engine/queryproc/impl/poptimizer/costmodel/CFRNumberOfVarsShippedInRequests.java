package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;

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

		if ( lop instanceof LogicalOpGPAdd gpAdd ) {
			numberOfVars = gpAdd.getPattern().getNumberOfVarMentions();

			final PhysicalPlan subplan = plan.getSubPlan(0);
			final PhysicalPlan reqGP = PhysicalPlanFactory.extractRequestAsPlan(gpAdd);
			numberOfJoinVars = PhysicalPlanUtils.intersectionOfCertainVariables(subplan,reqGP).size();

			if ( pop instanceof PhysicalOpBindJoinWithVALUES ) {
				futureIntResSize = null; // irrelevant
			} else {
				futureIntResSize = initiateCardinalityEstimation(subplan);
			}
		}
		else if ( lop instanceof LogicalOpRequest reqOp ) {
			final DataRetrievalRequest req = reqOp.getRequest();
			if ( req instanceof TriplePatternRequest tpReq )
				numberOfVars = tpReq.getQueryPattern().getNumberOfVarMentions();
			else if ( req instanceof SPARQLRequest sparqlReq )
				numberOfVars = sparqlReq.getQueryPattern().getNumberOfVarMentions();
			else
				throw createIllegalArgumentException(req);

			numberOfJoinVars = 0;    // irrelevant for request operators
			futureIntResSize = null; // irrelevant for request operators
		}
		else if (    lop instanceof LogicalOpJoin
		          || lop instanceof LogicalOpUnion
		          || lop instanceof LogicalOpMultiwayUnion
		          || lop instanceof LogicalOpLocalToGlobal
		          || lop instanceof LogicalOpGlobalToLocal ) {
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
		else if (    pop instanceof BaseForPhysicalOpBinaryJoin
		          || pop instanceof PhysicalOpBinaryUnion
		          || pop instanceof PhysicalOpMultiwayUnion
		          || pop instanceof PhysicalOpGlobalToLocal
		          || pop instanceof PhysicalOpLocalToGlobal ) {
			costValue = 0;
		}
		else {
			throw createIllegalArgumentException(pop);
		}

		return CompletableFuture.completedFuture(costValue);
	}

}
