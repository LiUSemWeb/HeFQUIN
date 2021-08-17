package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.ConstructSubPPBasedOnUnaryOperator;

public abstract class ConvertTPAddToBinaryOperator implements RewritingRule {
    protected final ConstructSubPPBasedOnUnaryOperator helper = new ConstructSubPPBasedOnUnaryOperator();

    @Override
    public PhysicalPlan applyTo( final PhysicalPlan plan, final PhysicalPlan[] subPlans ) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) pop.getLogicalOperator();

        final BinaryPhysicalOp popJoin = definePhysicalOperator();
        final PhysicalPlan subqueryRequest = helper.formRequestBasedOnTPofTPAdd( lop );
        final PhysicalPlan ppNew = new PhysicalPlanWithBinaryRootImpl( popJoin, subqueryRequest, subPlans[0]);

        return ppNew;
    }

    public abstract BinaryPhysicalOp definePhysicalOperator();

}
