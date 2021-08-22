package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.ConstructSubPPBasedOnUnaryOperator;

/**
 * This is a class for rule applications that convert any TPAdd operator to a binary operator.
 * Depending on the rule, the root operator of the new physical plan can be
 * hash join{@link ConvertTPAddToHashJoin},
 * symmetric hash join{@link ConvertTPAddToSymmetricHashJoin},
 * or naive nested loop join{@link ConvertTPAddToNaiveNLJ}.
 */

public abstract class ConvertTPAddToBinaryOperator implements RewritingRule {
    protected final ConstructSubPPBasedOnUnaryOperator helper = new ConstructSubPPBasedOnUnaryOperator();

    @Override
    public PhysicalPlan applyTo( final PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator popRoot = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) popRoot.getLogicalOperator();

        final BinaryPhysicalOp popJoin = definePhysicalOperator();
        final PhysicalPlan subqueryRequest = helper.formRequestBasedOnTPofTPAdd( lop );
        return new PhysicalPlanWithBinaryRootImpl( popJoin, subqueryRequest, plan.getSubPlan(0));
    }

    public abstract BinaryPhysicalOp definePhysicalOperator();

}
