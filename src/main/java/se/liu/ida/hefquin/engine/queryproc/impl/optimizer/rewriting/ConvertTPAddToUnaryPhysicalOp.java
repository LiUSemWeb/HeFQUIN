package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithUnaryRootImpl;

/**
 * This is a class for rule applications that convert any TPAdd operator to a unary physical operator.
 * Depending on the rule, the root operator of the new physical plan can be
 * bind join{@link ConvertTPAddToBindJoin},
 * bind join with FILTER{@link ConvertTPAddToBindJoinWithFILTER},
 * bind join with UNION{@link ConvertTPAddToBindJoinWithUNION},
 * bind join with VALUES{@link ConvertTPAddToBindJoinWithVALUES},
 * and index nested loop join{@link ConvertTPAddToIndexNestedLoopJoin},
 */

public abstract class ConvertTPAddToUnaryPhysicalOp implements RewritingRule{

    @Override
    public PhysicalPlan applyTo( final PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator popRoot = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) popRoot.getLogicalOperator();

        final UnaryPhysicalOp popUnary = definePhysicalOperator( lop );
        return new PhysicalPlanWithUnaryRootImpl( popUnary, plan.getSubPlan(0) );
    }

    public abstract UnaryPhysicalOp definePhysicalOperator( final LogicalOpTPAdd lop );

}
