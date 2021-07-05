package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.federation.*;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;

import java.util.*;

public class CardinalityEstimation {
    protected final PhysicalPlanCardinalityTable ppCardinalityTable = new PhysicalPlanCardinalityTable();
    protected final PhysicalPlanVarSpecificCardinalityTable ppVarCardinalityTable = new PhysicalPlanVarSpecificCardinalityTable();

    public int getCardinalityEstimationOfLeafNode( final PhysicalPlan pp, final FederationAccessManager mgr ) throws FederationAccessException {
        if ( ppCardinalityTable.contains(pp) ){ return ppCardinalityTable.get(pp); }

        assertEquals( 0, pp.numberOfSubPlans() );
        final PhysicalOperator lop = pp.getRootOperator();
        assertTrue( lop instanceof PhysicalOpRequest );

        final DataRetrievalRequest req = ((PhysicalOpRequest<?, ?>) lop).getLogicalOperator().getRequest();
        final FederationMember fm = ((PhysicalOpRequest<?, ?>) lop).getLogicalOperator().getFederationMember();

        final int cardinality;
        if ( fm instanceof SPARQLEndpoint && req instanceof SPARQLRequest ) {
            final CardinalityResponse resp = mgr.performCardinalityRequest( (SPARQLRequest) req, (SPARQLEndpoint) fm );
            cardinality = resp.getCardinality();
        } else if ( fm instanceof TPFServer && req instanceof TPFRequest ) {
            final TPFResponse resp = mgr.performRequest( (TPFRequest) req, (TPFServer) fm );
            cardinality = resp.getMetadataSize();
        } else if ( fm instanceof BRTPFServer && req instanceof TPFRequest ) {
            final TPFResponse resp = mgr.performRequest( (TPFRequest) req, (BRTPFServer) fm );
            cardinality = resp.getMetadataSize();
        } else if ( fm instanceof BRTPFServer && req instanceof BRTPFRequest ) {
            final TPFResponse resp = mgr.performRequest( (BRTPFRequest) req, (BRTPFServer) fm );
            cardinality = resp.getMetadataSize();
        } else
            throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");

        ppCardinalityTable.add( pp, cardinality );
        return cardinality;
    }

    public int getJoinCardinalityEstimation( final PhysicalPlan pp, final FederationAccessManager mgr ) throws FederationAccessException {
        if ( ppCardinalityTable.contains(pp) ){ return ppCardinalityTable.get(pp); }

        assertEquals( 2, pp.numberOfSubPlans() );
        final PhysicalOperatorForLogicalOperator lop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        assertTrue( lop.getLogicalOperator() instanceof LogicalOpJoin );

        final PhysicalPlan pp1 = pp.getSubPlan(0);
        final PhysicalPlan pp2 = pp.getSubPlan(1);

        final int cardinality = joinCardinality( pp1, pp2, mgr );
        ppCardinalityTable.add(pp, cardinality);

        return cardinality;
    }

    public int getTPAddCardinalityEstimation( final PhysicalPlan pp, final FederationAccessManager mgr ) throws FederationAccessException {
        if (ppCardinalityTable.contains(pp)){ return ppCardinalityTable.get(pp); }

        assertEquals( 1, pp.numberOfSubPlans() );
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();
        assert ( lop instanceof LogicalOpTPAdd );

        final PhysicalPlan pp1 = pp.getSubPlan(0);
        final PhysicalPlan reqTP = formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );

        final int cardinality = joinCardinality( pp1, reqTP, mgr );
        ppCardinalityTable.add( pp, cardinality );

        return cardinality;
    }

    public int getBGPAddCardinalityEstimation( final PhysicalPlan pp, final FederationAccessManager mgr ) throws FederationAccessException {
        if ( ppCardinalityTable.contains(pp) ){ return ppCardinalityTable.get(pp); }

        assertEquals( 1, pp.numberOfSubPlans() );
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();
        assert ( lop instanceof LogicalOpBGPAdd );

        final PhysicalPlan pp1 = pp.getSubPlan(0);
        final PhysicalPlan reqBGP = formRequestBasedOnBGPofBGPAdd( (LogicalOpBGPAdd) lop );

        final int cardinality = joinCardinality( pp1, reqBGP, mgr );
        ppCardinalityTable.add( pp, cardinality );

        return cardinality;
    }

    private int joinCardinality( final PhysicalPlan pp1, final PhysicalPlan pp2, final FederationAccessManager mgr ) throws FederationAccessException {
        final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( pp1.getExpectedVariables(), pp2.getExpectedVariables() );
        final Set<Var> possibleJoinVars = ExpectedVariablesUtils.intersectionOfPossibleVariables( pp1.getExpectedVariables(), pp2.getExpectedVariables() );
        final Set<Var> allCertainVars = ExpectedVariablesUtils.unionOfCertainVariables( pp1.getExpectedVariables(), pp2.getExpectedVariables() );

        int cardinality = 0;

        if ( !certainJoinVars.isEmpty() ){
            for ( final Var v : certainJoinVars ) {
                int c1 = getCardinalityEstimationOfSpecificVar( pp1, v, mgr );
                int c2 = getCardinalityEstimationOfSpecificVar( pp2, v, mgr );
                int c = min(c1, c2);
                cardinality = max(c, cardinality);
            }
        }
        else if ( !possibleJoinVars.isEmpty() ){
            for ( final Var v : possibleJoinVars ) {
                int c1 = getCardinalityEstimationOfSpecificVar( pp1, v, mgr );
                int c2 = getCardinalityEstimationOfSpecificVar( pp2, v, mgr );
                int c = min(c1, c2);
                cardinality = max(c, cardinality);
            }
        }
        else {
            for ( final Var v : allCertainVars ) {
                int c ;
                if ( pp1.getExpectedVariables().getCertainVariables().contains(v) ){
                    c = getCardinalityEstimationOfSpecificVar( pp1, v, mgr );
                }
                else c = getCardinalityEstimationOfSpecificVar( pp2, v, mgr );
                cardinality = max(c, cardinality);
            }
        }

        return cardinality;
    }

    public int getCardinalityEstimationOfSpecificVar( final PhysicalPlan pp, final Var v, final FederationAccessManager mgr ) throws FederationAccessException {
        if ( ppVarCardinalityTable.contains(pp, v) ){ return ppVarCardinalityTable.get(pp, v); }

        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();

        int cardinality = 0;
        if (lop instanceof LogicalOpRequest) {
            cardinality = getCardinalityEstimationOfLeafNode(pp, mgr);
        }
        else if (lop instanceof LogicalOpTPAdd) {
            final PhysicalPlan pp1 = pp.getSubPlan(0);
            final PhysicalPlan reqTP = formRequestBasedOnTPofTPAdd((LogicalOpTPAdd) lop);

            return joinCardinalityBasedOnVar( pp1, reqTP, v, mgr );
        }
        else if (lop instanceof LogicalOpBGPAdd){
            final PhysicalPlan pp1 = pp.getSubPlan(0);
            final PhysicalPlan reqBGP = formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd)lop);

            return joinCardinalityBasedOnVar( pp1, reqBGP, v, mgr );
        }
        else if (lop instanceof LogicalOpJoin){
            final PhysicalPlan pp1 = pp.getSubPlan(0);
            final PhysicalPlan pp2 = pp.getSubPlan(1);

            return joinCardinalityBasedOnVar( pp1, pp2, v, mgr );
        }
        else if (lop instanceof LogicalOpUnion){
            final PhysicalPlan pp1 = pp.getSubPlan(0);
            final PhysicalPlan pp2 = pp.getSubPlan(1);

            final int c1 = getCardinalityEstimationOfSpecificVar( pp1, v, mgr );
            final int c2 = getCardinalityEstimationOfSpecificVar( pp2, v, mgr );

            cardinality = c1 + c2;
        }

        ppVarCardinalityTable.add(pp, v, cardinality);
        return cardinality;
    }

    private int joinCardinalityBasedOnVar( PhysicalPlan pp1, PhysicalPlan pp2, Var v, FederationAccessManager mgr ) throws FederationAccessException {
        final int cardinality;
        final int c1 = getCardinalityEstimationOfSpecificVar( pp1, v, mgr );
        final int c2 = getCardinalityEstimationOfSpecificVar( pp2, v, mgr );

        final Set<Var> allJoinVars = ExpectedVariablesUtils.intersectionOfAllVariables( pp1.getExpectedVariables(), pp2.getExpectedVariables() );

        if ( allJoinVars.contains(v) ){ cardinality = min(c1, c2); }
        else cardinality = c1 * c2;
        return cardinality;
    }

    // helper function
    public PhysicalPlan formRequestBasedOnTPofTPAdd( final LogicalOpTPAdd lop ){
        final FederationMember fm = lop.getFederationMember();

        final DataRetrievalRequest req;
        if ( fm instanceof SPARQLEndpoint ) {
            req = new TriplePatternRequestImpl( lop.getTP());
        }
        else if ( fm instanceof TPFServer ) {
            req = new TPFRequestImpl(lop.getTP(), 0);
        }
        else if ( fm instanceof BRTPFServer ) {
            req = new TPFRequestImpl(lop.getTP(), 0);
        }
        else
            throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() );

        final LogicalOpRequest<?,?> op = new LogicalOpRequest<>( fm, req );
        final PhysicalPlan pp = new PhysicalPlanWithNullaryRootImpl( new PhysicalOpRequest(op) );

        return pp;
    }

    public PhysicalPlan formRequestBasedOnBGPofBGPAdd( final LogicalOpBGPAdd lop ){
        final FederationMember fm = lop.getFederationMember();

        final DataRetrievalRequest req;
        if ( fm.getInterface().supportsBGPRequests()) {
            req = new BGPRequestImpl( lop.getBGP());
        }
        else
            throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() );

        final LogicalOpRequest<?,?> op = new LogicalOpRequest<>( fm, req );
        final PhysicalPlan pp = new PhysicalPlanWithNullaryRootImpl(new PhysicalOpRequest(op));

        return pp;
    }

    public PhysicalPlan formRequestBasedOnPattern( final SPARQLGraphPattern P, final FederationMember fm ){
        final DataRetrievalRequest req;

        if ( fm instanceof SPARQLEndpoint) {
            req = new SPARQLRequestImpl( P );
        }
        else if ( fm instanceof TPFServer ) {
            req = new TriplePatternRequestImpl((TriplePattern) P);
        }
        else if ( fm instanceof BRTPFServer ) {
            req = new TriplePatternRequestImpl((TriplePattern) P);
        }
        else
            throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() );

        final LogicalOpRequest<?,?> op = new LogicalOpRequest<>( fm, req );
        final PhysicalPlan pp = new PhysicalPlanWithNullaryRootImpl( new PhysicalOpRequest(op) );

        return pp;
    }

}