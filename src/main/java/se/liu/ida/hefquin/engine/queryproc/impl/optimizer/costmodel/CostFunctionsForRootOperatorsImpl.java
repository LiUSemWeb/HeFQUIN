package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.cardinality.CardinalityEstimationHelper;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CostFunctionsForRootOperatorsImpl implements CostFunctionsForRootOperators {
    protected final CardinalityEstimation cardEstimate;

    public CostFunctionsForRootOperatorsImpl( final CardinalityEstimation cardEstimate ) {
        assert cardEstimate != null;
        this.cardEstimate = cardEstimate;
    }

    @Override
    public CompletableFuture<Integer> determineNumberOfRequests( final PhysicalPlan pp ) {
        final PhysicalOperator pop = pp.getRootOperator();

        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin ) {
        	return determineIntermediateResultsSize(pp.getSubPlan(0));
        }

        final int result;
        if ( pop instanceof PhysicalOpBindJoin || pop instanceof PhysicalOpBindJoinWithUNION || pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES ) {
            // TODO: Returning 1 is not entirely correct here. The actual number of requests depends on the page size used for the bind-join requests.
        	result = 1;
        } else if ( pop instanceof PhysicalOpRequest ) {
            // TODO: Returning 1 is not entirely correct here. The actual number of requests depends on the page size used for the requests.
        	result = 1;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
        	result = 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Integer> determineShippedRDFTermsForRequests( final PhysicalPlan pp ) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        final int numberOfTerms ;
        final int numberOfJoinVars;
        final CompletableFuture<Integer> futureIntResSize;

        if ( lop instanceof LogicalOpTPAdd){
            numberOfTerms = 3 - ((LogicalOpTPAdd) lop).getTP().numberOfVars();
            final PhysicalPlan subPP = pp.getSubPlan(0);
            futureIntResSize = determineIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestPlan( (LogicalOpTPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpBGPAdd){
            numberOfTerms = numberOfTermsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            final PhysicalPlan subPP = pp.getSubPlan(0);
            futureIntResSize = determineIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestPlan( (LogicalOpBGPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpRequest) {
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();
            if ( req instanceof SPARQLRequest) {
                numberOfTerms = numberOfTermsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
            } else if ( req instanceof TriplePatternRequest) {
                numberOfTerms = 3 - ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
            } else{
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");
            }
            numberOfJoinVars = 0;    // irrelevant for request operators
            futureIntResSize = null; // irrelevant for request operators
        } else if ( lop instanceof LogicalOpJoin){
            numberOfTerms = 0;
            // TODO I added the following line to be able to use 'final' for
            // this program variable. However, I wonder: shouldn't the number
            // of join variables be determined here too?
            numberOfJoinVars = 0;
            futureIntResSize = null; // irrelevant for join operators
        } else{
            throw new IllegalArgumentException("Unsupported Logical Operator");
        }

        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin || pop instanceof PhysicalOpBindJoinWithUNION ){
            return futureIntResSize.thenApply( intResSize -> intResSize * (numberOfTerms - numberOfJoinVars) );
        } else if ( pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES || pop instanceof PhysicalOpBindJoin ){
            return futureIntResSize.thenApply( intResSize -> numberOfTerms + intResSize * numberOfJoinVars );
        } else if ( pop instanceof PhysicalOpRequest ) {
            return CompletableFuture.completedFuture( numberOfTerms );
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
            return CompletableFuture.completedFuture( numberOfTerms );
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    @Override
    public CompletableFuture<Integer> determineShippedVarsForRequests( final PhysicalPlan pp ) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        final int numberOfVars;
        final int numberOfJoinVars;
        final CompletableFuture<Integer> futureIntResSize;

        if ( lop instanceof LogicalOpTPAdd ){
            numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
            final PhysicalPlan subPP = pp.getSubPlan(0);
            futureIntResSize = determineIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestPlan( (LogicalOpTPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpBGPAdd ){
            numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            final PhysicalPlan subPP = pp.getSubPlan(0);
            futureIntResSize = determineIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestPlan( (LogicalOpBGPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpRequest ) {
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();
            if ( req instanceof SPARQLRequest) {
                numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
            } else if ( req instanceof TriplePatternRequest) {
                numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
            } else{
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");
            }
            numberOfJoinVars = 0;    // irrelevant for request operators
            futureIntResSize = null; // irrelevant for request operators 
        } else if ( lop instanceof LogicalOpJoin){
            numberOfVars = 0;
            // TODO I added the following line to be able to use 'final' for
            // this program variable. However, I wonder: shouldn't the number
            // of join variables be determined here too?
            numberOfJoinVars = 0;
            futureIntResSize = null; // irrelevant for join operators
        } else{
            throw new IllegalArgumentException("Unsupported Logical Operator");
        }

        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin || pop instanceof PhysicalOpBindJoinWithUNION ){
            return futureIntResSize.thenApply( intResSize -> intResSize * (numberOfVars - numberOfJoinVars) );
        } else if ( pop instanceof PhysicalOpBindJoinWithFILTER  || pop instanceof PhysicalOpBindJoin ){
            return futureIntResSize.thenApply( intResSize -> numberOfVars + intResSize * numberOfJoinVars );
        } else if ( pop instanceof PhysicalOpBindJoinWithVALUES ){
            return CompletableFuture.completedFuture( numberOfVars + numberOfJoinVars );
        } else if ( pop instanceof PhysicalOpRequest ) {
            return CompletableFuture.completedFuture( numberOfVars );
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
            return CompletableFuture.completedFuture( numberOfVars );
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");

    }

    @Override
    public CompletableFuture<Integer> determineShippedRDFTermsForResponses( final PhysicalPlan pp ) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint) {
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                return determineIntermediateResultsSize(pp).thenApply( resSize -> numberOfVars * resSize );
            }
            else if ( fm instanceof TPFServer || fm instanceof BRTPFServer) {
                return determineIntermediateResultsSize(pp).thenApply( resSize -> 3 * resSize );
            }
            else {
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );
            }
        }
        else if ( lop instanceof LogicalOpBGPAdd ) {
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ) {
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                return determineIntermediateResultsSize(pp).thenApply( resSize -> numberOfVars * resSize );
            }
            else {
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );
            }
        }
        else if ( lop instanceof LogicalOpRequest ) {
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();

            if ( req instanceof SPARQLRequest ) {
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                return determineIntermediateResultsSize(pp).thenApply( resSize -> numberOfVars * resSize );
            }
            else if ( req instanceof TriplePatternRequest ) {
                final int numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
                return CompletableFuture.completedFuture( 3 * numberOfVars );
            }
            else {
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");
            }
        }
        else if ( lop instanceof LogicalOpJoin ) {
            return CompletableFuture.completedFuture( 0 );
        }
        else {
            throw new IllegalArgumentException("Unsupported type of root operator (" + lop.getClass().getName() + ").");
        }
    }

    @Override
    public CompletableFuture<Integer> determineShippedVarsForResponses( final PhysicalPlan pp ) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        if ( lop instanceof LogicalOpTPAdd ) {
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ) {
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                return determineIntermediateResultsSize(pp).thenApply( resSize -> numberOfVars * resSize );
            }
            else if ( fm instanceof TPFServer || fm instanceof BRTPFServer ) {
                return CompletableFuture.completedFuture( 0 );
            }
            else {
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );
            }
        }
        else if ( lop instanceof LogicalOpBGPAdd ) {
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ) {
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                return determineIntermediateResultsSize(pp).thenApply( resSize -> numberOfVars * resSize );
            }
            else {
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );
            }
        }
        else if ( lop instanceof LogicalOpRequest ) {
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();

            if ( req instanceof SPARQLRequest ) {
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                return determineIntermediateResultsSize(pp).thenApply( resSize -> numberOfVars * resSize );
            }
            // TODO There is a mistake here: every TriplePatternRequest is a SPARQLRequest;
            // hence, the following else-if branch will never be used
            else if ( req instanceof TriplePatternRequest ) {
                return CompletableFuture.completedFuture( 0 );
            }
            else {
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");
            }
        }
        else if ( lop instanceof LogicalOpJoin ) {
            return CompletableFuture.completedFuture( 0 );
        }
        else {
            throw new IllegalArgumentException("Unsupported type of root operator (" + lop.getClass().getName() + ").");
        }
    }

    @Override
    public CompletableFuture<Integer> determineIntermediateResultsSize( final PhysicalPlan plan ) {
    	return cardEstimate.initiateCardinalityEstimation(plan);
    }

    // helper functions
    protected int numberOfTermsOfBGP( final BGP bgp ){
        final int numberOfVars = numberOfVarsOfBGP(bgp);

        final Set<? extends TriplePattern> tps= bgp.getTriplePatterns();
        return (3 * tps.size() - numberOfVars);
    }

    protected int numberOfVarsOfBGP( final BGP bgp ){
        final Set<? extends TriplePattern > tps= bgp.getTriplePatterns();
        final Iterator<? extends TriplePattern> it = tps.iterator();
        int numberOfVars = 0;
        while ( it.hasNext() ){
            numberOfVars = numberOfVars + it.next().numberOfVars();
        }
        return numberOfVars;
    }
}
