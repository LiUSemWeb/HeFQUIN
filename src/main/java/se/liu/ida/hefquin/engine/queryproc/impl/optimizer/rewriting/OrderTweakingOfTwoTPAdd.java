package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithUnaryRootImpl;

public abstract class OrderTweakingOfTwoTPAdd implements RewritingRule {

    @Override
    public PhysicalPlan applyTo( final PhysicalPlan plan, final PhysicalPlan[] subPlans ) {
        final PhysicalOperatorForLogicalOperator popRoot = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        final LogicalOpTPAdd lopRoot = (LogicalOpTPAdd) popRoot.getLogicalOperator();

        final PhysicalOperatorForLogicalOperator popInner = (PhysicalOperatorForLogicalOperator) subPlans[0].getRootOperator();
        final LogicalOpTPAdd lopInner = (LogicalOpTPAdd) popInner.getLogicalOperator();

        final UnaryPhysicalOp popNewInner = defineInnerPop( lopInner );
        final UnaryPhysicalOp popNewRoot = defineRootPop( lopRoot );

        final PhysicalPlan ppInner = new PhysicalPlanWithUnaryRootImpl( popNewInner, subPlans[0].getSubPlan(0));
        final PhysicalPlan ppNew = new PhysicalPlanWithUnaryRootImpl( popNewRoot, ppInner);

        return ppNew;
    }

    public abstract UnaryPhysicalOp defineInnerPop( final LogicalOpTPAdd lop );
    public abstract UnaryPhysicalOp defineRootPop( final LogicalOpTPAdd lop );

}
