package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.federation.*;
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
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

import java.util.Iterator;
import java.util.Set;

public class CostModel extends CardinalityEstimation{
    protected final QueryProcContext ctxt;

    public CostModel(QueryProcContext ctxt) {
        super(ctxt);
        this.ctxt = ctxt;
    }

    public void measureCost(final PhysicalPlan pp) throws FederationAccessException {
        // wrap up
    }

    public int numberOfRequests(final PhysicalOperator pop, final PhysicalPlan subPhysicalPlan ) throws FederationAccessException, QueryOptimizationException {
        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin ) {
            return getIntermediateResultsSize(subPhysicalPlan);
        }
        else if ( pop instanceof PhysicalOpBindJoin || pop instanceof PhysicalOpBindJoinWithUNION || pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES){
            return 1;
        }
        else if ( pop instanceof PhysicalOpRequest) {
            return 1;
        }
        else if ( pop instanceof BasePhysicalOpBinaryJoin){
            return 0;
        }
        else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    public int shippedRDFTermsForRequests (final PhysicalOperator pop, final PhysicalPlan pp) throws FederationAccessException, QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator popConvert = (PhysicalOperatorForLogicalOperator) pop;
        final LogicalOperator lop = popConvert.getLogicalOperator();

        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin || pop instanceof PhysicalOpBindJoinWithUNION ) {
            final PhysicalPlan pp1 = pp.getSubPlan(0);

            if ( lop instanceof LogicalOpTPAdd ){
                final int numberOfTerms = 3 - ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                final PhysicalPlan reqTP = formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );

                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                final int size = getIntermediateResultsSize(pp1)* (numberOfTerms + certainJoinVars.size());
                return size;
            }
            else if (lop instanceof LogicalOpBGPAdd){
                final int numberOfTerms = numberOfTermsOfBGP(((LogicalOpBGPAdd) lop).getBGP());

                final PhysicalPlan reqTP = formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                final int size = getIntermediateResultsSize(pp1)* (numberOfTerms + certainJoinVars.size());
                return size;
            }
        }
        else if ( pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES || pop instanceof PhysicalOpBindJoin){
            final PhysicalPlan pp1 = pp.getSubPlan(0);

            if ( lop instanceof LogicalOpTPAdd ){
                final int numberOfTerms = 3 - ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                final PhysicalPlan reqTP = formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );

                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                final int size = numberOfTerms + getIntermediateResultsSize(pp1) * certainJoinVars.size();
                return size;
            }
            else if (lop instanceof LogicalOpBGPAdd){
                final int numberOfTerms = numberOfTermsOfBGP(((LogicalOpBGPAdd) lop).getBGP());

                final PhysicalPlan reqTP = formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                final int size = numberOfTerms + getIntermediateResultsSize(pp1) * certainJoinVars.size();
                return size;
            }
        }
        else if ( pop instanceof PhysicalOpRequest) {
            if ( lop instanceof LogicalOpTPAdd ){
                return ( 3 - ((LogicalOpTPAdd) lop).getTP().numberOfVars() );
            }
            else if (lop instanceof LogicalOpBGPAdd){
                return numberOfTermsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            }
        }
        else if ( pop instanceof BasePhysicalOpBinaryJoin){
            return 0;
        }
        else
            throw new IllegalArgumentException("Unsupported Physical Operator");
        return 0;
    }

    public int shippedRDFVarsForRequests (final PhysicalOperator pop, final PhysicalPlan pp) throws FederationAccessException, QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator popConvert = (PhysicalOperatorForLogicalOperator) pop;
        final LogicalOperator lop = popConvert.getLogicalOperator();

        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin || pop instanceof PhysicalOpBindJoinWithUNION ) {
            final PhysicalPlan pp1 = pp.getSubPlan(0);

            if ( lop instanceof LogicalOpTPAdd ){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                final PhysicalPlan reqTP = formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );

                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                final int size = getIntermediateResultsSize(pp1)* (numberOfVars - certainJoinVars.size());
                return size;
            }
            else if (lop instanceof LogicalOpBGPAdd){
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());

                final PhysicalPlan reqTP = formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                final int size = getIntermediateResultsSize(pp1)* (numberOfVars - certainJoinVars.size());
                return size;
            }
        }
        else if ( pop instanceof PhysicalOpBindJoinWithFILTER  || pop instanceof PhysicalOpBindJoin ){
            final PhysicalPlan pp1 = pp.getSubPlan(0);

            if ( lop instanceof LogicalOpTPAdd ){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                final PhysicalPlan reqTP = formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );

                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                final int size = numberOfVars + getIntermediateResultsSize(pp1) * certainJoinVars.size();
                return size;
            }
            else if (lop instanceof LogicalOpBGPAdd){
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());

                final PhysicalPlan reqTP = formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                final int size = numberOfVars + getIntermediateResultsSize(pp1) * certainJoinVars.size();
                return size;
            }
        }
        else if ( pop instanceof PhysicalOpBindJoinWithVALUES ){
            final PhysicalPlan pp1 = pp.getSubPlan(0);

            if ( lop instanceof LogicalOpTPAdd ){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                final PhysicalPlan reqTP = formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );

                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                return numberOfVars + certainJoinVars.size();
            }
            else if (lop instanceof LogicalOpBGPAdd){
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());

                final PhysicalPlan reqTP = formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
                final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), reqTP.getExpectedVariables() );
                return numberOfVars + certainJoinVars.size();
            }
        }
        else if ( pop instanceof PhysicalOpRequest) {
            if ( lop instanceof LogicalOpTPAdd ){
                return ((LogicalOpTPAdd) lop).getTP().numberOfVars();
            }
            else if (lop instanceof LogicalOpBGPAdd){
                return numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            }
        }
        else if ( pop instanceof BasePhysicalOpBinaryJoin){
            return 0;
        }
        else
            throw new IllegalArgumentException("Unsupported Physical Operator");
        return 0;
    }

    public int shippedRDFTermsForResponses (final PhysicalOperator pop, final PhysicalPlan pp) throws FederationAccessException, QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator popConvert = (PhysicalOperatorForLogicalOperator) pop;
        final LogicalOperator lop = popConvert.getLogicalOperator();

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();
            if ( fm instanceof SPARQLEndpoint) {
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                return numberOfVars * getIntermediateResultsSize(pp);
            }
            else if ( fm instanceof TPFServer || fm instanceof BRTPFServer ) {
                return 3 * getIntermediateResultsSize(pp);
            }
            else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );
        }
        else if (lop instanceof LogicalOpBGPAdd){
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();
            if ( fm instanceof SPARQLEndpoint) {
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                return numberOfVars * getIntermediateResultsSize(pp);
            }
            else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );
        }
        else if ( lop instanceof LogicalOpRequest) {
            final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();
            if ( fm instanceof SPARQLEndpoint && req instanceof SPARQLRequest) {
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                return numberOfVars * getIntermediateResultsSize(pp);
            }
            else if ( fm instanceof TPFServer && req instanceof TriplePatternRequest) {
                final int numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
                return 3 * numberOfVars;
            }
            else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
                final int numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
                return 3 * numberOfVars;
            }
            else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
                final int numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
                return 3 * numberOfVars;
            }
            else
                throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
        }
        else if ( pop instanceof BasePhysicalOpBinaryJoin){
            return 0;
        }
        else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    public int shippedRDFVarsForResponses (final PhysicalOperator pop, final PhysicalPlan pp) throws FederationAccessException, QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator popConvert = (PhysicalOperatorForLogicalOperator) pop;
        final LogicalOperator lop = popConvert.getLogicalOperator();

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();
            if ( fm instanceof SPARQLEndpoint) {
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                return numberOfVars * getIntermediateResultsSize(pp);
            }
            else if ( fm instanceof TPFServer || fm instanceof BRTPFServer ) {
                return 0;
            }
            else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );
        }
        else if (lop instanceof LogicalOpBGPAdd){
            final FederationMember fm = ((LogicalOpBGPAdd) lop).getFederationMember();
            if ( fm instanceof SPARQLEndpoint) {
                final int numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
                return numberOfVars * getIntermediateResultsSize(pp);
            }
            else
                throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );
        }
        else if ( lop instanceof LogicalOpRequest) {
            final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();
            final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();
            if ( fm instanceof SPARQLEndpoint && req instanceof SPARQLRequest) {
                final int numberOfVars = numberOfVarsOfBGP((BGP) ((SPARQLRequest) req).getQueryPattern());
                return numberOfVars * getIntermediateResultsSize(pp);
            }
            else if ( fm instanceof TPFServer && req instanceof TriplePatternRequest) {
                final int numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
                return 0;
            }
            else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
                final int numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
                return 0;
            }
            else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
                final int numberOfVars = ((TriplePatternRequest) req).getQueryPattern().numberOfVars();
                return 0;
            }
            else
                throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
        }
        else if ( pop instanceof BasePhysicalOpBinaryJoin){
            return 0;
        }
        else
            throw new IllegalArgumentException("Unsupported Physical Operator");

    }

    public int getIntermediateResultsSize(final PhysicalPlan pp) throws FederationAccessException, QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        int cardinality = 0;
        if ( lop instanceof LogicalOpRequest) {
            cardinality = getCardinalityEstimationOfLeafNode( pp );
        }
        else if (lop instanceof LogicalOpJoin){
            cardinality = getJoinCardinalityEstimation( pp );
        }
        else if (lop instanceof LogicalOpTPAdd){
            cardinality = getTPAddCardinalityEstimation( pp );
        }
        else if (lop instanceof LogicalOpBGPAdd){
            cardinality = getBGPAddCardinalityEstimation( pp);
        }
        return cardinality;
    }

    // helper functions
    public int numberOfTermsOfBGP( final BGP bgp ){
        final Set<? extends TriplePattern > tps= bgp.getTriplePatterns();
        final int numberOfVars = numberOfVarsOfBGP(bgp);

        return (3 * tps.size() - numberOfVars);
    }

    public int numberOfVarsOfBGP( final BGP bgp ){
        final Set<? extends TriplePattern > tps= bgp.getTriplePatterns();
        final Iterator<? extends TriplePattern> it = tps.iterator();
        int numberOfVars = 0;
        while (it.hasNext()){
            numberOfVars = numberOfVars + it.next().numberOfVars();
        }
        return numberOfVars;
    }

}