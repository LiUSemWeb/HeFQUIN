package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeJoinOfOneTPReqIntoTPAdd extends AbstractRewritingRuleImpl{

    public RuleMergeJoinOfOneTPReqIntoTPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.matchJoin(rootOp) ) {
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

                if ( IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest( subPlan1.getRootOperator() ) ) {
                    final LogicalOperator subPlanLop1 = ((PhysicalOperatorForLogicalOperator) subPlan1.getRootOperator()).getLogicalOperator();
                    final LogicalOpTPAdd logicalTPAdd = ConstructUnaryLogicalOpFromReq.constructTPAddLopFromReq( (TriplePatternRequest) subPlanLop1 );
                    return PhysicalPlanFactory.createPlan(logicalTPAdd, subPlan2);
                }
                else if ( IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest( subPlan2.getRootOperator() ) ) {
                    final LogicalOperator subPlanLop2 = ((PhysicalOperatorForLogicalOperator) subPlan2.getRootOperator()).getLogicalOperator();
                    final LogicalOpTPAdd logicalTPAdd = ConstructUnaryLogicalOpFromReq.constructTPAddLopFromReq( (TriplePatternRequest) subPlanLop2 );
                    return PhysicalPlanFactory.createPlan(logicalTPAdd, subPlan1);
                }
                else  {
                    return plan;
                }
            }
        };
    }

}
