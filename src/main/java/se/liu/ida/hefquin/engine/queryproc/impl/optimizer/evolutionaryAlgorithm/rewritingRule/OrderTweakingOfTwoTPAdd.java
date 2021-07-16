package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithUnaryRootImpl;

public abstract class OrderTweakingOfTwoTPAdd implements Rule {

    @Override
    public PhysicalPlan applyTo( final PhysicalPlan pp ) {
        final PhysicalOperatorForLogicalOperator popRoot = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOpTPAdd lopRoot = (LogicalOpTPAdd) popRoot.getLogicalOperator();

        final PhysicalPlan subPlan = pp.getSubPlan(0);
        final PhysicalOperatorForLogicalOperator popInner = (PhysicalOperatorForLogicalOperator) subPlan.getRootOperator();
        final LogicalOpTPAdd lopInner = (LogicalOpTPAdd) popInner.getLogicalOperator();

        final UnaryPhysicalOp popNewInner = defineInnerPop( lopInner );
        final UnaryPhysicalOp popNewRoot = defineRootPop( lopRoot );

        final PhysicalPlan ppInner = new PhysicalPlanWithUnaryRootImpl( popNewInner, subPlan.getSubPlan(0));
        final PhysicalPlan ppNew = new PhysicalPlanWithUnaryRootImpl( popNewRoot, ppInner);

        return ppNew;
    }

    @Override
    public Double getPriority() {
        return 0.25;
    }

    public abstract UnaryPhysicalOp defineInnerPop( final LogicalOpTPAdd lop );
    public abstract UnaryPhysicalOp defineRootPop( final LogicalOpTPAdd lop );

}
