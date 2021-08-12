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
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CardinalityEstimationImpl implements CardinalityEstimation
{
    protected final CardinalitiesCache cardinalitiesCache;
    protected final VarSpecificCardinalitiesCache varSpecificCardinalitiesCache;
    protected final QueryProcContext ctxt;

    public CardinalityEstimationImpl( final CardinalitiesCache cardinalitiesCache,
                                      final VarSpecificCardinalitiesCache varSpecificCardinalitiesCache,
                                      final QueryProcContext ctxt ) {
        assert ctxt != null;
        this.ctxt = ctxt;

        if ( cardinalitiesCache != null ) {
            this.cardinalitiesCache = cardinalitiesCache;
        }
        else {
            this.cardinalitiesCache = new CardinalitiesCache();
        }

        if ( varSpecificCardinalitiesCache != null ) {
            this.varSpecificCardinalitiesCache = varSpecificCardinalitiesCache;
        }
        else {
            this.varSpecificCardinalitiesCache = new VarSpecificCardinalitiesCache();
        }
    }

    public CardinalityEstimationImpl( final QueryProcContext ctxt ) {
        this(null, null, ctxt);
    }

    @Override
    public int getCardinalityEstimation( final PhysicalPlan plan )
    		throws CardinalityEstimationException
    {
        final Integer cachedCard = cardinalitiesCache.get(plan);
        if ( cachedCard != null ) {
            return cachedCard;
        }

        final int result;
        final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
        if ( rootOp instanceof LogicalOpRequest ){
        	result = getCardinalityEstimationOfLeafNode(plan);
        }
        else if ( rootOp instanceof LogicalOpJoin ){
        	result = getJoinCardinalityEstimation(plan);
        }
        else if ( rootOp instanceof LogicalOpTPAdd ){
        	result = getTPAddCardinalityEstimation(plan);
        }
        else if ( rootOp instanceof LogicalOpBGPAdd ){
        	result = getBGPAddCardinalityEstimation(plan);
        }
        else {
            throw new IllegalArgumentException("The type of the root operator of the given plan is current not supported (" + rootOp.getClass().getName() + ").");
        }

        cardinalitiesCache.add(plan, result);
        return result;
    }

    protected int getCardinalityEstimationOfLeafNode( final PhysicalPlan plan ) throws CardinalityEstimationException {
        final PhysicalOperator rootOp = plan.getRootOperator();
        final DataRetrievalRequest req = ((PhysicalOpRequest<?, ?>) rootOp).getLogicalOperator().getRequest();
        final FederationMember fm = ((PhysicalOpRequest<?, ?>) rootOp).getLogicalOperator().getFederationMember();

        final CompletableFuture<CardinalityResponse> futureCR;
        try {
            if ( req instanceof SPARQLRequest && fm instanceof SPARQLEndpoint ) {
                futureCR = ctxt.getFederationAccessMgr().issueCardinalityRequest( (SPARQLRequest) req, (SPARQLEndpoint) fm );
            }
            else if ( req instanceof TPFRequest && fm instanceof TPFServer ) {
                futureCR = ctxt.getFederationAccessMgr().issueCardinalityRequest( (TPFRequest) req, (TPFServer) fm );
            }
            else if ( req instanceof TPFRequest && fm instanceof BRTPFServer ) {
                futureCR = ctxt.getFederationAccessMgr().issueCardinalityRequest( (TPFRequest) req, (BRTPFServer) fm );
            }
            else if ( req instanceof BRTPFRequest && fm instanceof BRTPFServer ) {
                futureCR = ctxt.getFederationAccessMgr().issueCardinalityRequest( (BRTPFRequest) req, (BRTPFServer) fm );
            }
            else {
                throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
            }
        }
        catch ( final FederationAccessException e ) {
            throw new CardinalityEstimationException("Issuing a cardinality request caused an exception.", e, plan);
        }

        final CardinalityResponse cr;
        try {
            cr = futureCR.get();
        }
        catch ( final InterruptedException e ) {
            throw new CardinalityEstimationException("Performing a cardinality request caused an exception.", e, plan);
        }
        catch ( final ExecutionException e ) {
            throw new CardinalityEstimationException("Performing a cardinality request caused an exception.", e, plan);
        }

        return cr.getCardinality();
    }

    protected int getJoinCardinalityEstimation( final PhysicalPlan plan )
    		throws CardinalityEstimationException
    {
        return joinCardinality( plan.getSubPlan(0), plan.getSubPlan(1) );
    }

    protected int getTPAddCardinalityEstimation( final PhysicalPlan plan )
    		throws CardinalityEstimationException
    {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
        final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestBasedOnTPofTPAdd( (LogicalOpTPAdd) lop );

        return joinCardinality( plan.getSubPlan(0), reqTP );
    }

    protected int getBGPAddCardinalityEstimation( final PhysicalPlan plan )
    		throws CardinalityEstimationException
    {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
        final PhysicalPlan reqBGP = CardinalityEstimationHelper.formRequestBasedOnBGPofBGPAdd( (LogicalOpBGPAdd) lop );

        return joinCardinality( plan.getSubPlan(0), reqBGP );
    }

    protected int joinCardinality( final PhysicalPlan pp1, final PhysicalPlan pp2 ) throws CardinalityEstimationException {
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

    protected int getCardinalityEstimationOfSpecificVar( final PhysicalPlan pp, final Var v ) throws CardinalityEstimationException {
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
            final PhysicalPlan reqTP = CardinalityEstimationHelper.formRequestBasedOnTPofTPAdd((LogicalOpTPAdd) lop);

            return joinCardinalityBasedOnVar( pp1, reqTP, v );
        } else if ( lop instanceof LogicalOpBGPAdd ){
            final PhysicalPlan pp1 = pp.getSubPlan(0);
            final PhysicalPlan reqBGP = CardinalityEstimationHelper.formRequestBasedOnBGPofBGPAdd((LogicalOpBGPAdd)lop);

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

    protected int joinCardinalityBasedOnVar( PhysicalPlan pp1, PhysicalPlan pp2, Var v ) throws CardinalityEstimationException {
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


}
