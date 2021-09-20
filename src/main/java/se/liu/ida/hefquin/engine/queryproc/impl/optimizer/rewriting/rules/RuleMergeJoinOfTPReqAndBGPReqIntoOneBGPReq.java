package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RuleMergeJoinOfTPReqAndBGPReqIntoOneBGPReq extends AbstractRewritingRuleImpl{

    public RuleMergeJoinOfTPReqAndBGPReqIntoOneBGPReq( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        if (rootOp.getLogicalOperator() instanceof LogicalOpJoin) {

            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            if ( isTriplePatternRequest( subPlanOp1 ) ) {
                final FederationMember fm = ((LogicalOpRequest) ((PhysicalOperatorForLogicalOperator)subPlanOp1).getLogicalOperator()).getFederationMember();

                return ( fm instanceof SPARQLEndpoint) && isBGPRequestWithSameFm( subPlanOp2, fm );
            }
            else if ( isBGPRequest( subPlanOp1 ) ){
                final FederationMember fm = ((LogicalOpRequest) ((PhysicalOperatorForLogicalOperator)subPlanOp1).getLogicalOperator()).getFederationMember();

                return ( fm instanceof SPARQLEndpoint) && isTriplePatternRequestWithSameFm( subPlanOp2, fm );
            }
            else {
                return false;
            }
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                PhysicalPlan subPlan1 = plan.getSubPlan(0);
                PhysicalPlan subPlan2 = plan.getSubPlan(1);

                final LogicalOperator subPlanLop1 = ((PhysicalOperatorForLogicalOperator) subPlan1.getRootOperator()).getLogicalOperator();
                final LogicalOperator subPlanLop2 = ((PhysicalOperatorForLogicalOperator) subPlan2.getRootOperator()).getLogicalOperator();

                final BGP newBGP = createNewBGP( subPlanLop1, subPlanLop2);
                final DataRetrievalRequest newReq = new BGPRequestImpl( newBGP );

                final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop1).getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

    protected boolean isTriplePatternRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof TriplePatternRequest;
        }
        return false;
    }

    protected boolean isTriplePatternRequestWithSameFm( final PhysicalOperator op, final FederationMember fm ) {
        if ( isTriplePatternRequest(op) ){
            final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
            return ((LogicalOpRequest)lop).getFederationMember() == fm;
        }
        return false;
    }

    protected boolean isBGPRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof BGPRequest;
        }
        return false;
    }

    protected boolean isBGPRequestWithSameFm( final PhysicalOperator op, final FederationMember fm ) {
        if ( isBGPRequest(op) ){
            final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
            return ((LogicalOpRequest)lop).getFederationMember() == fm;
        }
        return false;
    }

    protected BGP createNewBGP(final LogicalOperator lop1, final LogicalOperator lop2 ) {
        final Set<TriplePattern> tps = getTriplePatterns(lop1);
        tps.addAll( getTriplePatterns(lop2) );

        return new BGPImpl(tps);
    }

    protected Set<TriplePattern> getTriplePatterns( final LogicalOperator lop ) {
        final DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop).getRequest();
        if ( req instanceof TriplePatternRequest ) {
            final TriplePatternRequest tpReq = (TriplePatternRequest) ((LogicalOpRequest<?, ?>) lop).getRequest();
            final TriplePattern tp = tpReq.getQueryPattern();

            return (Set<TriplePattern>) tp;
        }
        else if ( req instanceof BGPRequest) {
            final BGPRequest bgpReq = (BGPRequest) ((LogicalOpRequest<?, ?>) lop).getRequest();
            final BGP bgp = bgpReq.getQueryPattern();

            return (Set<TriplePattern>) bgp.getTriplePatterns();
        }
        else  {
            return null;
        }
    }
}
