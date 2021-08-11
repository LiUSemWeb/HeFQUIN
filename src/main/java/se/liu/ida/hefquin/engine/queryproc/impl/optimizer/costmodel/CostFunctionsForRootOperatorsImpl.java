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
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimationHelper;

import java.util.Iterator;
import java.util.Set;

public class CostFunctionsForRootOperatorsImpl implements CostFunctionsForRootOperators {
    protected final CardinalityEstimation cardEstimate;

    public CostFunctionsForRootOperatorsImpl( final CardinalityEstimation cardEstimate ) {
        assert cardEstimate != null;
        this.cardEstimate = cardEstimate;
    }

    @Override
    public int determineNumberOfRequests( final PhysicalPlan pp ) throws CostEstimationException {
        final PhysicalOperator pop = pp.getRootOperator();
        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin ) {
            return determineIntermediateResultsSize(pp.getSubPlan(0));
        } else if ( pop instanceof PhysicalOpBindJoin || pop instanceof PhysicalOpBindJoinWithUNION || pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES ) {
            // TODO: Returning 1 is not entirely correct here. The actual number of requests depends on the page size used for the bind-join requests.
            return 1;
        } else if ( pop instanceof PhysicalOpRequest ) {
            // TODO: Returning 1 is not entirely correct here. The actual number of requests depends on the page size used for the requests.
            return 1;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
            return 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");

    }

    @Override
    public int determineShippedRDFTermsForRequests( final PhysicalPlan pp ) throws CostEstimationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();
        int numberOfTerms = 0;
        int intermediateResultSize = 0 ;
        int numberOfJoinVars = 0;

        if ( lop instanceof LogicalOpTPAdd){
            numberOfTerms = 3 - ((LogicalOpTPAdd) lop).getTP().numberOfVars();
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = determineIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpBGPAdd){
            numberOfTerms = numberOfTermsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = determineIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
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
        } else if ( lop instanceof LogicalOpJoin){
            numberOfTerms = 0;
        } else{
            throw new IllegalArgumentException("Unsupported Logical Operator");
        }

        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin || pop instanceof PhysicalOpBindJoinWithUNION ){
            return intermediateResultSize * (numberOfTerms + numberOfJoinVars);
        } else if ( pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES || pop instanceof PhysicalOpBindJoin ){
            return numberOfTerms + intermediateResultSize * numberOfJoinVars;
        } else if ( pop instanceof PhysicalOpRequest ) {
            return numberOfTerms;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin ) {
            return numberOfTerms;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    @Override
    public int determineShippedVarsForRequests( final PhysicalPlan pp ) throws CostEstimationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        int numberOfVars = 0;
        int intermediateResultSize = 0 ;
        int numberOfJoinVars = 0;

        if ( lop instanceof LogicalOpTPAdd ){
            numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = determineIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpBGPAdd ){
            numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = determineIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
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
        } else if ( lop instanceof LogicalOpJoin){
            numberOfVars = 0;
        } else{
            throw new IllegalArgumentException("Unsupported Logical Operator");
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
            return numberOfVars;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");

    }

    @Override
    public int determineShippedRDFTermsForResponses( final PhysicalPlan pp ) throws CostEstimationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                return numberOfVars * determineIntermediateResultsSize(pp);
            } else if ( fm instanceof TPFServer || fm instanceof BRTPFServer){
                return 3 * determineIntermediateResultsSize(pp);
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpBGPAdd ){
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                return numberOfVars * determineIntermediateResultsSize(pp);
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpRequest ){
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();

            if ( req instanceof SPARQLRequest ){
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                return numberOfVars * determineIntermediateResultsSize(pp);
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
    public int determineShippedVarsForResponses( final PhysicalPlan pp ) throws CostEstimationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                return numberOfVars * determineIntermediateResultsSize(pp);
            } else if ( fm instanceof TPFServer || fm instanceof BRTPFServer ){
                return 0;
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpBGPAdd ){
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                return numberOfVars * determineIntermediateResultsSize(pp);
            } else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        } else if ( lop instanceof LogicalOpRequest ){
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();

            if ( req instanceof SPARQLRequest ){
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                return numberOfVars * determineIntermediateResultsSize(pp);
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
    public int determineIntermediateResultsSize(final PhysicalPlan pp) throws CostEstimationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        try {
            if ( lop instanceof LogicalOpRequest ){
                return cardEstimate.getCardinalityEstimationOfLeafNode( pp );
            } else if ( lop instanceof LogicalOpJoin ){
                return cardEstimate.getJoinCardinalityEstimation( pp );
            } else if ( lop instanceof LogicalOpTPAdd ){
                return cardEstimate.getTPAddCardinalityEstimation( pp );
            } else if ( lop instanceof LogicalOpBGPAdd ){
                return cardEstimate.getBGPAddCardinalityEstimation( pp);
            } else
                throw new IllegalArgumentException("Unsupported Logical Operator");
        }
        catch ( final CardinalityEstimationException e ) {
            throw new CostEstimationException("Performing cardinality estimation caused an exception.", e, pp);
        }
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
