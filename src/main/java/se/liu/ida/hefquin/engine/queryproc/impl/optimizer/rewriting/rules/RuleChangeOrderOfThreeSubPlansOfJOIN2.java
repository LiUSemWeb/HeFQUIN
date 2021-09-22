package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleChangeOrderOfThreeSubPlansOfJOIN2 extends AbstractRewritingRuleImpl{

    public RuleChangeOrderOfThreeSubPlansOfJOIN2( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        // root operator is JOIN
        // one of the sub plans has join as root, the other sub plan is req
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.matchJoin(rootOp) ) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            return ( IdentifyLogicalOp.matchJoin(subPlanOp1) && IdentifyPhysicalOpUsedForReq.isTriplePatternRequest(subPlanOp2) )
                    ||( IdentifyLogicalOp.matchJoin(subPlanOp2) && IdentifyPhysicalOpUsedForReq.isTriplePatternRequest(subPlanOp1) );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final BinaryPhysicalOp rootOp = (BinaryPhysicalOp) plan.getRootOperator();

                final PhysicalPlan subPlan1 = plan.getSubPlan(0);
                final PhysicalPlan subPlan2 = plan.getSubPlan(1);
                final PhysicalOperator subPlanOp1 = subPlan1.getRootOperator();
                final PhysicalOperator subPlanOp2 = subPlan2.getRootOperator();

                if ( IdentifyLogicalOp.matchJoin(subPlanOp1) && IdentifyPhysicalOpUsedForReq.isTriplePatternRequest(subPlanOp2) ) {
                    final UnaryLogicalOp tpAdd = ConstructUnaryLogicalOpFromReq.constructUnaryLopFromReq(subPlanOp2);
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( tpAdd, subPlan1.getSubPlan(1) );

                    return PhysicalPlanFactory.createPlan( rootOp, subPlan1.getSubPlan(0), newSubPlan);
                }
                else if ( IdentifyLogicalOp.matchJoin(subPlanOp2) && IdentifyPhysicalOpUsedForReq.isTriplePatternRequest(subPlanOp1) ) {
                    final UnaryLogicalOp tpAdd = ConstructUnaryLogicalOpFromReq.constructUnaryLopFromReq(subPlanOp1);
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan(tpAdd, subPlan2.getSubPlan(0));

                    return PhysicalPlanFactory.createPlan( rootOp, newSubPlan, subPlan2.getSubPlan(1));
                }
                else  {
                    return plan;
                }
            }
        };
    }

}