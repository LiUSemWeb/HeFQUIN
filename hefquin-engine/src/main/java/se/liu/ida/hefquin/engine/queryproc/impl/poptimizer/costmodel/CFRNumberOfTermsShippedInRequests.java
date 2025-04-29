package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;

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

		if ( lop instanceof LogicalOpTPAdd || lop instanceof LogicalOpBGPAdd || lop instanceof LogicalOpGPAdd  ) {
			final SPARQLGraphPattern pattern;
			if ( lop instanceof LogicalOpTPAdd tpAdd )
				pattern = tpAdd.getTP();
			else if ( lop instanceof LogicalOpBGPAdd bgpAdd )
				pattern = bgpAdd.getBGP();
			else if ( lop instanceof LogicalOpGPAdd gpAdd )
				pattern = gpAdd.getPattern();
			else
				throw createIllegalArgumentException(lop);

			numberOfTerms = pattern.getNumberOfTermMentions();

			final PhysicalPlan subplan = plan.getSubPlan(0);
			final PhysicalPlan req = PhysicalPlanFactory.extractRequestAsPlan( (UnaryLogicalOp) lop );
			numberOfJoinVars = PhysicalPlanUtils.intersectionOfCertainVariables(subplan, req).size();

			futureIntResSize = initiateCardinalityEstimation(subplan);
		}
		else if ( lop instanceof LogicalOpRequest reqOp ) {
			final DataRetrievalRequest req = reqOp.getRequest();
			if ( req instanceof TriplePatternRequest tpReq )
				numberOfTerms = tpReq.getQueryPattern().getNumberOfTermMentions();
			else if ( req instanceof SPARQLRequest sparqlReq )
				numberOfTerms = sparqlReq.getQueryPattern().getNumberOfTermMentions();
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
			return futureIntResSize.thenApply( intResSize -> intResSize * (numberOfTerms + numberOfJoinVars) );
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
		else if (    pop instanceof BaseForPhysicalOpBinaryJoin
		          || pop instanceof PhysicalOpBinaryUnion
		          || pop instanceof PhysicalOpMultiwayUnion
		          || pop instanceof PhysicalOpLocalToGlobal
		          || pop instanceof PhysicalOpGlobalToLocal ) {
			costValue = 0;
		}
		else {
			throw createIllegalArgumentException(pop);
		}

		return CompletableFuture.completedFuture(costValue);
	}

}
