package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

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

import java.util.*;

public class CostModel extends CardinalityEstimation{
    protected final QueryProcContext ctxt;
    protected final RootOperatorCostCache rootOperatorCostCache = new RootOperatorCostCache();

    public CostModel(QueryProcContext ctxt) {
        super(ctxt);
        this.ctxt = ctxt;
    }

    public Double overallCost( final PhysicalPlan pp ) throws QueryOptimizationException {
        List<Integer> measureMetrics = costMetrics( pp, new ArrayList<Integer>() );
        ArrayList<Double> weight= new ArrayList<Double>(Arrays.asList(0.2, 0.2, 0.2, 0.2, 0.2));

        double overallCost = 0;
        for (  int i = 0; i < measureMetrics.size(); i++ ){
            overallCost = overallCost + measureMetrics.get(i) * weight.get(i);
        }

        return overallCost;
    }

    protected List<Integer> costMetrics( final PhysicalPlan pp, final List<Integer> overallMetrics ) throws QueryOptimizationException {
        final List<PhysicalPlan> children = findChildren(pp);

        for ( int i = 0; i < children.size(); i++ ){
            final List<Integer> li = metricsOfRootOperator( children.get(i) );
            for ( int j = 0; j < li.size(); j++ ){
                overallMetrics.set( j, overallMetrics.get(j) + li.get(j) ) ;
            }
            costMetrics( children.get(i), overallMetrics);
        }

        return overallMetrics;
    }

    protected List<PhysicalPlan> findChildren(final PhysicalPlan pp ) throws QueryOptimizationException {
        final List<PhysicalPlan> children = new ArrayList<PhysicalPlan>();
        final int numChildren = pp.numberOfSubPlans();

        for ( int i = 0; i < numChildren; ++i ){
            children.add( pp.getSubPlan(i) );
        }
        return children;
    }

    protected List<Integer> metricsOfRootOperator(final PhysicalPlan pp ) throws QueryOptimizationException {
        final List<Integer> metrics = new ArrayList<Integer>();

        metrics.add( numberOfRequests( pp.getRootOperator(), pp ) );
        metrics.add( shippedRDFTermsForRequests( pp.getRootOperator(), pp ) );
        metrics.add( shippedRDFVarsForRequests( pp.getRootOperator(), pp ) );
        metrics.add( shippedRDFTermsForResponses( pp.getRootOperator(), pp ) );
        metrics.add( shippedRDFVarsForResponses( pp.getRootOperator(), pp ) );

        rootOperatorCostCache.add( pp, metrics );
        return metrics;
    }

    protected int numberOfRequests( final PhysicalOperator pop, final PhysicalPlan pp ) throws QueryOptimizationException {
        if ( pop instanceof PhysicalOpIndexNestedLoopsJoin ){
            return getIntermediateResultsSize(pp.getSubPlan(0));
        } else if ( pop instanceof PhysicalOpBindJoin || pop instanceof PhysicalOpBindJoinWithUNION || pop instanceof PhysicalOpBindJoinWithFILTER || pop instanceof PhysicalOpBindJoinWithVALUES ){
            return 1;
        } else if ( pop instanceof PhysicalOpRequest){
            return 1;
        } else if ( pop instanceof BasePhysicalOpBinaryJoin){
            return 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    protected int shippedRDFTermsForRequests ( final PhysicalOperator pop, final PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator popConvert = (PhysicalOperatorForLogicalOperator) pop;
        final LogicalOperator lop = popConvert.getLogicalOperator();

        int numberOfTerms = 0;
        int intermediateResultSize = 0 ;
        int numberOfJoinVars = 0;

        if ( lop instanceof LogicalOpTPAdd ){
            numberOfTerms = 3 - ((LogicalOpTPAdd) lop).getTP().numberOfVars();
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = getIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpBGPAdd ){
            numberOfTerms = numberOfTermsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = getIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpRequest ) {
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

    protected int shippedRDFVarsForRequests (final PhysicalOperator pop, final PhysicalPlan pp) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator popConvert = (PhysicalOperatorForLogicalOperator) pop;
        final LogicalOperator lop = popConvert.getLogicalOperator();

        int numberOfVars = 0;
        int intermediateResultSize = 0 ;
        int numberOfJoinVars = 0;

        if ( lop instanceof LogicalOpTPAdd ){
            numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = getIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );
            numberOfJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( subPP.getExpectedVariables(), reqTP.getExpectedVariables() ).size();
        } else if ( lop instanceof LogicalOpBGPAdd ){
            numberOfVars = numberOfVarsOfBGP(((LogicalOpBGPAdd) lop).getBGP());
            final PhysicalPlan subPP = pp.getSubPlan(0);
            intermediateResultSize = getIntermediateResultsSize(subPP);

            final PhysicalPlan reqTP = formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd) lop);
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

    protected int shippedRDFTermsForResponses (final PhysicalOperator pop, final PhysicalPlan pp) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator popConvert = (PhysicalOperatorForLogicalOperator) pop;
        final LogicalOperator lop = popConvert.getLogicalOperator();

        if ( lop instanceof LogicalOpTPAdd ){
            final FederationMember fm = ((LogicalOpTPAdd) lop).getFederationMember();

            if ( fm instanceof SPARQLEndpoint ){
                final int numberOfVars = ((LogicalOpTPAdd) lop).getTP().numberOfVars();
                return numberOfVars * getIntermediateResultsSize(pp);
            } else if ( fm instanceof TPFServer || fm instanceof BRTPFServer ){
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

        } else if ( lop instanceof LogicalOpJoin ){
            return 0;
        } else
            throw new IllegalArgumentException("Unsupported Physical Operator");
    }

    protected int shippedRDFVarsForResponses ( final PhysicalOperator pop, final PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator popConvert = (PhysicalOperatorForLogicalOperator) pop;
        final LogicalOperator lop = popConvert.getLogicalOperator();

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

    protected int getIntermediateResultsSize(final PhysicalPlan pp) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        int cardinality = 0;
        if ( lop instanceof LogicalOpRequest ){
            cardinality = getCardinalityEstimationOfLeafNode( pp );
        } else if ( lop instanceof LogicalOpJoin ){
            cardinality = getJoinCardinalityEstimation( pp );
        } else if ( lop instanceof LogicalOpTPAdd ){
            cardinality = getTPAddCardinalityEstimation( pp );
        } else if ( lop instanceof LogicalOpBGPAdd ){
            cardinality = getBGPAddCardinalityEstimation( pp);
        }
        return cardinality;
    }

    // helper functions
    protected int numberOfTermsOfBGP( final BGP bgp ){
        final int numberOfVars = numberOfVarsOfBGP(bgp);

        final Set<? extends TriplePattern > tps= bgp.getTriplePatterns();
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