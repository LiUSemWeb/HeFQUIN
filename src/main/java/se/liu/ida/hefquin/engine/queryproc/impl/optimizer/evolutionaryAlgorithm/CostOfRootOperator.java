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

public class CostOfRootOperator implements Metrics{
    protected final CardinalityEstimation cardEstimate;

    public CostOfRootOperator( CardinalityEstimation cardEstimate ) {
        this.cardEstimate = cardEstimate;
    }

    @Override
    public int getNumberOfRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperator pop = pp.getRootOperator();

        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin){
            return getIntermediateResultsSize(pp.getSubPlan(0));
        } else if ( pop instanceof PhysicalOpBindJoin || pop instanceof PhysicalOpBindJoinWithUNION || pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES){
            return 1;
        } else if ( pop instanceof PhysicalOpRequest){
            return 1;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin){
            return 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    @Override
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

        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin || pop instanceof PhysicalOpBindJoinWithUNION ){
            return intermediateResultSize * (numberOfTerms + numberOfJoinVars);
        } else if ( pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES || pop instanceof PhysicalOpBindJoin ){
            return numberOfTerms + intermediateResultSize * numberOfJoinVars;
        } else if ( pop instanceof PhysicalOpRequest ) {
            return numberOfTerms;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
            return 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    @Override
    public int getShippedRDFVarsForRequests( PhysicalPlan pp ) throws QueryOptimizationException {
        //return 0;
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

        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin || pop instanceof PhysicalOpBindJoinWithUNION ){
            return intermediateResultSize * (numberOfVars - numberOfJoinVars);
        } else if ( pop instanceof PhysicalOpBindJoinWithFILTER  || pop instanceof PhysicalOpBindJoin ){
            return numberOfVars + intermediateResultSize * numberOfJoinVars;
        } else if ( pop instanceof PhysicalOpBindJoinWithVALUES ){
            return numberOfVars + numberOfJoinVars;
        } else if ( pop instanceof PhysicalOpRequest ) {
            return numberOfVars;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
            return 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    @Override
    public int getShippedRDFTermsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        //return 0;
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                return numberOfVars * getIntermediateResultsSize(pp);
            } else if ( fm instanceof TPFServer || fm instanceof BRTPFServer){
                return 3 * getIntermediateResultsSize(pp);
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpBGPAdd ){
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                return numberOfVars * getIntermediateResultsSize(pp);
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpRequest ){
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();

            if ( req instanceof SPARQLRequest ){
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                return numberOfVars * getIntermediateResultsSize(pp);
            } else if ( req instanceof TriplePatternRequest ){
                final int numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
                return 3 * numberOfVars;
            } else
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");

        } else if ( lop instanceof LogicalOpJoin){
            return 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    @Override
    public int getShippedRDFVarsForResponses( PhysicalPlan pp ) throws QueryOptimizationException {
        //return 0;
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                return numberOfVars * getIntermediateResultsSize(pp);
            } else if ( fm instanceof TPFServer || fm instanceof BRTPFServer ){
                return 0;
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpBGPAdd ){
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                return numberOfVars * getIntermediateResultsSize(pp);
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpRequest ){
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();

            if ( req instanceof SPARQLRequest ){
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                return numberOfVars * getIntermediateResultsSize(pp);
            } else if ( req instanceof TriplePatternRequest ){
                return 0;
            } else
                throw new IllegalArgumentException("Unsupported request type (" + req.getClass().getName() + ")");

        } else if ( lop instanceof LogicalOpJoin ){
            return 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    @Override
    public int getIntermediateResultsSize(final PhysicalPlan pp) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        int cardinality = 0;
        if ( lop instanceof LogicalOpRequest ){
            cardinality = cardEstimate.getCardinalityEstimationOfLeafNode( pp );
        } else if ( lop instanceof LogicalOpJoin ){
            cardinality = cardEstimate.getJoinCardinalityEstimation( pp );
        } else if ( lop instanceof LogicalOpTPAdd ){
            cardinality = cardEstimate.getTPAddCardinalityEstimation( pp );
        } else if ( lop instanceof LogicalOpBGPAdd ){
            cardinality = cardEstimate.getBGPAddCardinalityEstimation( pp);
        }
        return cardinality;
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
