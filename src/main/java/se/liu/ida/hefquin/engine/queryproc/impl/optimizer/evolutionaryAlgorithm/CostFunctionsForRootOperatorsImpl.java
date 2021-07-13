package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

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
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

import java.util.Iterator;
import java.util.Set;

public class CostFunctionsForRootOperatorsImpl implements CostFunctionsForRootOperators {
    protected final CardinalityEstimation cardEstimate;

    public CostFunctionsForRootOperatorsImpl( final CardinalityEstimation cardEstimate ) {
        assert cardEstimate != null;
        this.cardEstimate = cardEstimate;
    }

    @Override
    public int getNumberOfRequests( final PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperator pop = pp.getRootOperator();
        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin ) {
            return getIntermediateResultsSize(pp.getSubPlan(0));
        } else if ( pop instanceof PhysicalOpBindJoin || pop instanceof PhysicalOpBindJoinWithUNION || pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES ) {
            // TODO: Returning 1 is not entirely correct here. The actual number of requests depends on the page size used for the bind-join requests.
            return 1;
        } else if ( pop instanceof PhysicalOpRequest ) {
            // TODO: Returning 1 is not entirely correct here. The actual number of requests depends on the page size used for the requests.
            return 1;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
            return 0;
        } else {
            throw new IllegalArgumentException("Unsupported Physical Operator");
        }
    }

    public int getShippedRDFTermsForRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();
        int numberOfTerms = 0;
        int intermediateResultSize = 0 ;
        int numberOfJoinVars = 0;

        if ( lop instanceof LogicalOpTPAdd){
            numberOfTerms = 3 - ((LogicalOpTPAdd) lop).getTP().numberOfVars();
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = getIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = cardEstimate.formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpBGPAdd){
            numberOfTerms = numberOfTermsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = getIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = cardEstimate.formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpRequest) {
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();
            if ( req instanceof SPARQLRequest) {
                numberOfTerms = numberOfTermsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
            } else if ( req instanceof TriplePatternRequest) {
                numberOfTerms = 3 - ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
            } else
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");
        }

        final int cost;
        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin || pop instanceof PhysicalOpBindJoinWithUNION ){
            cost = intermediateResultSize * (numberOfTerms + numberOfJoinVars);
        } else if ( pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES || pop instanceof PhysicalOpBindJoin ){
            cost = numberOfTerms + intermediateResultSize * numberOfJoinVars;
        } else if ( pop instanceof PhysicalOpRequest ) {
            cost = numberOfTerms;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
            cost = 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");

        return cost;
    }

    public int getShippedRDFVarsForRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        int numberOfVars = 0;
        int intermediateResultSize = 0 ;
        int numberOfJoinVars = 0;

        if ( lop instanceof LogicalOpTPAdd ){
            numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = getIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = cardEstimate.formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpBGPAdd ){
            numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = getIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = cardEstimate.formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpRequest ) {
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();
            if ( req instanceof SPARQLRequest) {
                numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
            } else if ( req instanceof TriplePatternRequest) {
                numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
            } else
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");
        }

        final int cost;
        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin || pop instanceof PhysicalOpBindJoinWithUNION ){
            cost = intermediateResultSize * (numberOfVars - numberOfJoinVars);
        } else if ( pop instanceof PhysicalOpBindJoinWithFILTER  || pop instanceof PhysicalOpBindJoin ){
            cost = numberOfVars + intermediateResultSize * numberOfJoinVars;
        } else if ( pop instanceof PhysicalOpBindJoinWithVALUES ){
            cost = numberOfVars + numberOfJoinVars;
        } else if ( pop instanceof PhysicalOpRequest ) {
            cost = numberOfVars;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
            cost = 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");

        return cost;
    }

    public int getShippedRDFTermsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();
        final int cost;

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                cost = numberOfVars * getIntermediateResultsSize(pp);
            } else if ( fm instanceof TPFServer || fm instanceof BRTPFServer){
                cost = 3 * getIntermediateResultsSize(pp);
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpBGPAdd ){
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                cost = numberOfVars * getIntermediateResultsSize(pp);
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpRequest ){
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();

            if ( req instanceof SPARQLRequest ){
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                cost = numberOfVars * getIntermediateResultsSize(pp);
            } else if ( req instanceof TriplePatternRequest ){
                final int numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
                cost = 3 * numberOfVars;
            } else
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");

        } else if ( lop instanceof LogicalOpJoin){
            cost = 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");

        return cost;
    }

    public int getShippedRDFVarsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();
        final int cost;

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                cost = numberOfVars * getIntermediateResultsSize(pp);
            } else if ( fm instanceof TPFServer || fm instanceof BRTPFServer ){
                cost = 0;
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpBGPAdd ){
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                cost = numberOfVars * getIntermediateResultsSize(pp);
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpRequest ){
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();

            if ( req instanceof SPARQLRequest ){
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                cost = numberOfVars * getIntermediateResultsSize(pp);
            } else if ( req instanceof TriplePatternRequest ){
                cost = 0;
            } else
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");

        } else if ( lop instanceof LogicalOpJoin ){
            cost = 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");

        return cost;
    }

    public int getIntermediateResultsSize(final PhysicalPlan pp) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        int cost = 0;
        if ( lop instanceof LogicalOpRequest ){
            cost = cardEstimate.getCardinalityEstimationOfLeafNode( pp );
        } else if ( lop instanceof LogicalOpJoin ){
            cost = cardEstimate.getJoinCardinalityEstimation( pp );
        } else if ( lop instanceof LogicalOpTPAdd ){
            cost = cardEstimate.getTPAddCardinalityEstimation( pp );
        } else if ( lop instanceof LogicalOpBGPAdd ){
            cost = cardEstimate.getBGPAddCardinalityEstimation( pp);
        }

        return cost;
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
