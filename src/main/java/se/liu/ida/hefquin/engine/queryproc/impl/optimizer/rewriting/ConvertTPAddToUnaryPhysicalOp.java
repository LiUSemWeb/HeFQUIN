package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithUnaryRootImpl;

public abstract class ConvertTPAddToUnaryPhysicalOp implements RewritingRule{

    @Override
    public PhysicalPlan applyTo(PhysicalPlan plan, final PhysicalPlan[] subPlans) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) pop.getLogicalOperator();

        final UnaryPhysicalOp popUnary = definePhysicalOperator( lop );
        final PhysicalPlan ppNew = new PhysicalPlanWithUnaryRootImpl( popUnary, subPlans[0]);

        return ppNew;
    }

    public abstract UnaryPhysicalOp definePhysicalOperator( final LogicalOpTPAdd lop );

}
