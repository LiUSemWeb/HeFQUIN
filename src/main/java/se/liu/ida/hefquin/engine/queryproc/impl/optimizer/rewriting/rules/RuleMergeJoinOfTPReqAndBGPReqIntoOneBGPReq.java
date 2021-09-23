package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeJoinOfTPReqAndBGPReqIntoOneBGPReq extends AbstractRewritingRuleImpl{

    public RuleMergeJoinOfTPReqAndBGPReqIntoOneBGPReq( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.matchJoin(rootOp) ) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final LogicalOperator lop1 =  ((PhysicalOperatorForLogicalOperator)subPlanOp1).getLogicalOperator();

            if ( IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest( lop1 ) ) {
                final FederationMember fm = ((LogicalOpRequest<?, ?>) lop1).getFederationMember();

                final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();
                return ( fm instanceof SPARQLEndpoint) && IdentifyTypeOfRequestUsedForReq.isBGPRequestWithFm( subPlanOp2, fm );
            }
            else if ( IdentifyTypeOfRequestUsedForReq.isBGPRequest( lop1 ) ){
                final FederationMember fm = ((LogicalOpRequest<?, ?>) lop1).getFederationMember();

                final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();
                return ( fm instanceof SPARQLEndpoint) && IdentifyTypeOfRequestUsedForReq.isTriplePatternRequestWithFm( subPlanOp2, fm );
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
                final PhysicalPlan subPlan1 = plan.getSubPlan(0);
                final PhysicalPlan subPlan2 = plan.getSubPlan(1);

                final LogicalOpRequest<?, ?> subPlanLop1 = (LogicalOpRequest<?, ?>) ((PhysicalOperatorForLogicalOperator) subPlan1.getRootOperator()).getLogicalOperator();
                final LogicalOpRequest<?, ?> subPlanLop2 = (LogicalOpRequest<?, ?>) ((PhysicalOperatorForLogicalOperator) subPlan2.getRootOperator()).getLogicalOperator();

                final BGP newBGP = GraphPatternConstructor.createNewBGP( subPlanLop1, subPlanLop2);
                final DataRetrievalRequest newReq = new BGPRequestImpl( newBGP );

                final FederationMember fm = subPlanLop1.getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

}
