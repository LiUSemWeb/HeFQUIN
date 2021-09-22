package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeJoinOfOneBGPReqIntoBGPAdd extends AbstractRewritingRuleImpl{

    public RuleMergeJoinOfOneBGPReqIntoBGPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.matchJoin(rootOp) ) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            return IdentifyPhysicalOpUsedForReq.isBGPRequest( subPlanOp1 )
                    || IdentifyPhysicalOpUsedForReq.isBGPRequest( subPlanOp2 );
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
                final PhysicalOperator subPlanOp1 = subPlan1.getRootOperator();
                final PhysicalOperator subPlanOp2 = subPlan2.getRootOperator();

                if ( IdentifyPhysicalOpUsedForReq.isBGPRequest( subPlanOp1 ) ) {
                    final UnaryLogicalOp bgpAdd = ConstructUnaryLogicalOpFromReq.constructUnaryLopFromReq( subPlanOp1 );
                    return PhysicalPlanFactory.createPlan(bgpAdd, subPlan2);
                }
                else if ( IdentifyPhysicalOpUsedForReq.isBGPRequest( subPlanOp2 ) ) {
                    final UnaryLogicalOp bgpAdd = ConstructUnaryLogicalOpFromReq.constructUnaryLopFromReq( subPlanOp2 );
                    return PhysicalPlanFactory.createPlan(bgpAdd, subPlan1);
                }
                else  {
                    return plan;
                }
            }
        };
    }

}