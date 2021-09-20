package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeJoinAndBGPReqIntoBGPAdd extends AbstractRewritingRuleImpl{

    public RuleMergeJoinAndBGPReqIntoBGPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        if (rootOp.getLogicalOperator() instanceof LogicalOpJoin) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            return isBGPRequest( subPlanOp1 )
                    || isBGPRequest( subPlanOp2 );
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

                if ( isBGPRequest( subPlan1.getRootOperator() ) ) {
                    final BGPRequest bgpReq = (BGPRequest) ((LogicalOpRequest<?, ?>) subPlanLop1).getRequest();
                    final BGP bgp = bgpReq.getQueryPattern();

                    final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop1).getFederationMember();
                    final LogicalOpBGPAdd logicalBGPAdd = new LogicalOpBGPAdd( fm, bgp );

                    return PhysicalPlanFactory.createPlan(logicalBGPAdd, subPlan2);
                }
                else if ( isBGPRequest( subPlan2.getRootOperator() ) ) {
                    final BGPRequest bgpReq = (BGPRequest) ((LogicalOpRequest<?, ?>) subPlanLop2).getRequest();
                    final BGP bgp = bgpReq.getQueryPattern();

                    final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop2).getFederationMember();
                    final LogicalOpBGPAdd logicalBGPAdd = new LogicalOpBGPAdd( fm, bgp );

                    return PhysicalPlanFactory.createPlan(logicalBGPAdd, subPlan1);
                }
                else  {
                    return plan;
                }
            }
        };
    }

    protected boolean isBGPRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof BGPRequest;
        }
        return false;
    }

}
