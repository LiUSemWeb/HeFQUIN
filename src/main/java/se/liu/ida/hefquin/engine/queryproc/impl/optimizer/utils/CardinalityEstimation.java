package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.federation.*;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.ConstructRequestBasedOnUnaryOperator;

import java.util.*;

public class CardinalityEstimation {
    protected final CardinalitiesCache cardinalitiesCache = new CardinalitiesCache();
    protected final VarSpecificCardinalitiesCache varSpecificCardinalitiesCache = new VarSpecificCardinalitiesCache();
    protected final ConstructRequestBasedOnUnaryOperator helper = new ConstructRequestBasedOnUnaryOperator();
    protected final QueryProcContext ctxt;

    public CardinalityEstimation(QueryProcContext ctxt) {
        assert ctxt != null;
        this.ctxt = ctxt;
    }

    public int getCardinalityEstimationOfLeafNode( final PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperator lop = pp.getRootOperator();
        if ( !(lop instanceof PhysicalOpRequest) ){
            throw new IllegalArgumentException();
        }
        
        final Integer cachedCard = cardinalitiesCache.get(pp);
        if ( cachedCard != null ){
                return cachedCard;
        }

        final DataRetrievalRequest req = ((PhysicalOpRequest<?, ?>) lop).getLogicalOperator().getRequest();
        final FederationMember fm = ((PhysicalOpRequest<?, ?>) lop).getLogicalOperator().getFederationMember();

        final int cardinality;
        if ( fm instanceof SPARQLEndpoint && req instanceof SPARQLRequest ){
            final CardinalityResponse resp;
            try {
                resp = ctxt.getFederationAccessMgr().performCardinalityRequest( (SPARQLRequest) req, (SPARQLEndpoint) fm );
            } catch (FederationAccessException e) {
                throw new QueryOptimizationException("Exception occurred during performing cardinality estimate of SPARQLRequest over SPARQLEndpoint", e);
            }
            cardinality = resp.getCardinality();
        } else if ( fm instanceof TPFServer && req instanceof TPFRequest ){
            final TPFResponse resp;
            try {
                resp = ctxt.getFederationAccessMgr().performRequest( (TPFRequest) req, (TPFServer) fm );
            } catch (FederationAccessException e) {
                throw new QueryOptimizationException("Exception occurred during performing cardinality estimate of TPFRequest over TPFServer", e);
            }
            cardinality = resp.getCardinalityEstimate();
        } else if ( fm instanceof BRTPFServer && req instanceof TPFRequest ){
            final TPFResponse resp;
            try {
                resp = ctxt.getFederationAccessMgr().performRequest( (TPFRequest) req, (BRTPFServer) fm );
            } catch (FederationAccessException e) {
                throw new QueryOptimizationException("Exception occurred during performing cardinality estimate of TPFRequest over BRTPFServer", e);
            }
            cardinality = resp.getCardinalityEstimate();
        } else if ( fm instanceof BRTPFServer && req instanceof BRTPFRequest ){
            final TPFResponse resp;
            try {
                resp = ctxt.getFederationAccessMgr().performRequest( (BRTPFRequest) req, (BRTPFServer) fm );
            } catch (FederationAccessException e) {
                throw new QueryOptimizationException("Exception occurred during performing cardinality estimate of BRTPFRequest over BRTPFServer", e);
            }
            cardinality = resp.getCardinalityEstimate();
        } else
            throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");

        cardinalitiesCache.add( pp, cardinality );
        return cardinality;
    }

    public int getJoinCardinalityEstimation( final PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator lop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        if ( !(lop.getLogicalOperator() instanceof LogicalOpJoin) ){
            throw new IllegalArgumentException();
        }

        final Integer cachedCard = cardinalitiesCache.get(pp);
        if ( cachedCard != null ){
            return cachedCard;
        }

        final PhysicalPlan pp1 = pp.getSubPlan(0);
        final PhysicalPlan pp2 = pp.getSubPlan(1);

        final int cardinality = joinCardinality( pp1, pp2 );
        cardinalitiesCache.add(pp, cardinality);

        return cardinality;
    }

    public int getTPAddCardinalityEstimation( final PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();
        if ( !(lop instanceof LogicalOpTPAdd) ){
            throw new IllegalArgumentException();
        }

        final Integer cachedCard = cardinalitiesCache.get(pp);
        if ( cachedCard != null ){
            return cachedCard;
        }

        final PhysicalPlan pp1 = pp.getSubPlan(0);
        final PhysicalPlan reqTP = helper.formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );

        final int cardinality = joinCardinality( pp1, reqTP );
        cardinalitiesCache.add( pp, cardinality );

        return cardinality;
    }

    public int getBGPAddCardinalityEstimation( final PhysicalPlan pp ) throws QueryOptimizationException {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();
        if ( !(lop instanceof LogicalOpBGPAdd) ){
            throw new IllegalArgumentException();
        }

        final Integer cachedCard = cardinalitiesCache.get(pp);
        if ( cachedCard != null ){
            return cachedCard;
        }

        final PhysicalPlan pp1 = pp.getSubPlan(0);
        final PhysicalPlan reqBGP = helper.formRequestBasedOnBGPofBGPAdd( (LogicalOpBGPAdd) lop );

        final int cardinality = joinCardinality( pp1, reqBGP );
        cardinalitiesCache.add( pp, cardinality );

        return cardinality;
    }

    protected int joinCardinality( final PhysicalPlan pp1, final PhysicalPlan pp2 ) throws QueryOptimizationException {
        final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), pp2.getExpectedVariables() );
        final Set<Var> possibleJoinVars = ExpectedVariablesUtils.unionOfAllVariables( pp1.getExpectedVariables(), pp2.getExpectedVariables() );
        possibleJoinVars.removeAll(certainJoinVars);
        final Set<Var> allCertainVars = ExpectedVariablesUtils.unionOfCertainVariables( pp1.getExpectedVariables(), pp2.getExpectedVariables() );

        int cardinality = 0;

        if ( !certainJoinVars.isEmpty() ){
            for ( final Var v : certainJoinVars ){
                int c1 = getCardinalityEstimationOfSpecificVar( pp1, v );
                int c2 = getCardinalityEstimationOfSpecificVar( pp2, v );
                int c = min(c1, c2);
                cardinality = max(c, cardinality);
            }
        } else if ( !possibleJoinVars.isEmpty() ){
            for ( final Var v : possibleJoinVars ){
                int c1 = getCardinalityEstimationOfSpecificVar( pp1, v );
                int c2 = getCardinalityEstimationOfSpecificVar( pp2, v );
                int c = min(c1, c2);
                cardinality = max(c, cardinality);
            }
        } else {
            for ( final Var v : allCertainVars ){
                int c ;
                if ( pp1.getExpectedVariables().getCertainVariables().contains(v) ){
                    c = getCardinalityEstimationOfSpecificVar( pp1, v );
                }
                else c = getCardinalityEstimationOfSpecificVar( pp2, v );
                cardinality = max(c, cardinality);
            }
        }

        return cardinality;
    }

    protected int getCardinalityEstimationOfSpecificVar( final PhysicalPlan pp, final Var v ) throws QueryOptimizationException {
        final Integer varCachedCard = varSpecificCardinalitiesCache.get(pp, v);
        if ( varCachedCard != null ) {
            return varCachedCard;
        }

        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        int cardinality = 0;
        if ( lop instanceof LogicalOpRequest ){
            cardinality = getCardinalityEstimationOfLeafNode(pp );
        } else if ( lop instanceof LogicalOpTPAdd ){
            final PhysicalPlan pp1 = pp.getSubPlan(0);
            final PhysicalPlan reqTP = helper.formRequestBasedOnTPofTPAdd((LogicalOpTPAdd) lop);

            return joinCardinalityBasedOnVar( pp1, reqTP, v );
        } else if ( lop instanceof LogicalOpBGPAdd ){
            final PhysicalPlan pp1 = pp.getSubPlan(0);
            final PhysicalPlan reqBGP = helper.formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd)lop);

            return joinCardinalityBasedOnVar( pp1, reqBGP, v );
        } else if ( lop instanceof LogicalOpJoin ){
            final PhysicalPlan pp1 = pp.getSubPlan(0);
            final PhysicalPlan pp2 = pp.getSubPlan(1);

            return joinCardinalityBasedOnVar( pp1, pp2, v );
        } else if ( lop instanceof LogicalOpUnion ){
            final PhysicalPlan pp1 = pp.getSubPlan(0);
            final PhysicalPlan pp2 = pp.getSubPlan(1);

            final int c1 = getCardinalityEstimationOfSpecificVar( pp1, v );
            final int c2 = getCardinalityEstimationOfSpecificVar( pp2, v );

            cardinality = c1 + c2;
        } else
            throw new IllegalArgumentException();

        varSpecificCardinalitiesCache.add(pp, v, cardinality);
        return cardinality;
    }

    protected int joinCardinalityBasedOnVar( PhysicalPlan pp1, PhysicalPlan pp2, Var v ) throws QueryOptimizationException {
        final int cardinality;
        final int c1 = getCardinalityEstimationOfSpecificVar( pp1, v );
        final int c2 = getCardinalityEstimationOfSpecificVar( pp2, v );

        final Set<Var> allJoinVars = ExpectedVariablesUtils.intersectionOfAllVariables( pp1.getExpectedVariables(), pp2.getExpectedVariables() );

        if ( allJoinVars.contains(v) ){
            cardinality = min(c1, c2);
        } else {
            cardinality = c1 * c2;
        }
        return cardinality;
    }

    // helper function
    /*
    public PhysicalPlan formRequestBasedOnTPofTPAdd( final LogicalOpTPAdd lop ){
        final FederationMember fm = lop.getFederationMember();

        final DataRetrievalRequest req;
        if ( fm instanceof SPARQLEndpoint ){
            req = new TriplePatternRequestImpl( lop.getTP());
        } else if ( fm instanceof TPFServer ){
            req = new TPFRequestImpl(lop.getTP(), 0);
        } else if ( fm instanceof BRTPFServer ){
            req = new TPFRequestImpl(lop.getTP(), 0);
        } else
            throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() );

        final LogicalOpRequest<?,?> op = new LogicalOpRequest<>( fm, req );
        final PhysicalPlan pp = new PhysicalPlanWithNullaryRootImpl( new PhysicalOpRequest(op) );

        return pp;
    }

    public PhysicalPlan formRequestBasedOnBGPofBGPAdd( final LogicalOpBGPAdd lop ){
        final FederationMember fm = lop.getFederationMember();

        final DataRetrievalRequest req;
        if ( fm.getInterface().supportsBGPRequests() ){
            req = new BGPRequestImpl( lop.getBGP());
        } else
            throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() );

        final LogicalOpRequest<?,?> op = new LogicalOpRequest<>( fm, req );
        final PhysicalPlan pp = new PhysicalPlanWithNullaryRootImpl(new PhysicalOpRequest(op));

        return pp;
    }

    public PhysicalPlan formRequestBasedOnPattern( final SPARQLGraphPattern P, final FederationMember fm ){
        final DataRetrievalRequest req;

        if ( fm instanceof SPARQLEndpoint ){
            req = new SPARQLRequestImpl( P );
        } else if ( fm instanceof TPFServer ){
            req = new TriplePatternRequestImpl((TriplePattern) P);
        } else if ( fm instanceof BRTPFServer ){
            req = new TriplePatternRequestImpl((TriplePattern) P);
        } else
            throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );

        final LogicalOpRequest<?,?> op = new LogicalOpRequest<>( fm, req );
        final PhysicalPlan pp = new PhysicalPlanWithNullaryRootImpl( new PhysicalOpRequest(op) );

        return pp;
    }
     */

}
