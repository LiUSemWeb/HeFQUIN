package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleChangeOrderOfThreeSubPlansOfJOIN1 extends AbstractRewritingRuleImpl{

    public RuleChangeOrderOfThreeSubPlansOfJOIN1( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        // root operator is JOIN, and one of the sub plans has JOIN as root
        final PhysicalOperator rootOp = plan.getRootOperator();
        final PhysicalOperatorForLogicalOperator popLop = (PhysicalOperatorForLogicalOperator) rootOp;
        if (popLop.getLogicalOperator() instanceof LogicalOpJoin) {
            PhysicalOperatorForLogicalOperator subPlanOp1 = (PhysicalOperatorForLogicalOperator) plan.getSubPlan(0).getRootOperator();
            PhysicalOperatorForLogicalOperator subPlanOp2 = (PhysicalOperatorForLogicalOperator) plan.getSubPlan(1).getRootOperator();

            return ( subPlanOp1.getLogicalOperator() instanceof LogicalOpJoin )
                    || ( subPlanOp2.getLogicalOperator() instanceof LogicalOpJoin );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final BinaryPhysicalOp rootOp = (BinaryPhysicalOp) plan.getRootOperator();

                PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
                PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();
                if ( ((PhysicalOperatorForLogicalOperator) subPlanOp1).getLogicalOperator() instanceof LogicalOpJoin ) {
                    PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( subPlanOp1, plan.getSubPlan(0).getSubPlan(1), plan.getSubPlan(1));
                    return PhysicalPlanFactory.createPlan( rootOp, plan.getSubPlan(0).getSubPlan(0), newSubPlan );
                }
                else if ( ((PhysicalOperatorForLogicalOperator) subPlanOp2).getLogicalOperator() instanceof LogicalOpJoin ) {
                    PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( subPlanOp2, plan.getSubPlan(0), plan.getSubPlan(1).getSubPlan(0));
                    return PhysicalPlanFactory.createPlan( rootOp, newSubPlan, plan.getSubPlan(1).getSubPlan(1) );
                }
                else  {
                    return plan;
                }
            }
        };
    }

}
