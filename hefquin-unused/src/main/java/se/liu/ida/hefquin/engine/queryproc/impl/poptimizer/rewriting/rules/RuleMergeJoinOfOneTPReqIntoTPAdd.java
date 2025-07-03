package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;

public class RuleMergeJoinOfOneTPReqIntoTPAdd extends AbstractRewritingRuleImpl{

    public RuleMergeJoinOfOneTPReqIntoTPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.isJoin(rootOp) ) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            return IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest( subPlanOp1 )
                    || IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest( subPlanOp2 );
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

                if ( IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest( subPlanOp1 ) ) {
                    final UnaryLogicalOp newRoot = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp( subPlanOp1 );
                    return PhysicalPlanFactory.createPlan(newRoot, subPlan2);
                }
                else if ( IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest( subPlanOp2 ) ) {
                    final UnaryLogicalOp newRoot = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp( subPlanOp2 );
                    return PhysicalPlanFactory.createPlan(newRoot, subPlan1);
                }
                else  {
                    return plan;
                }
            }
        };
    }

}
