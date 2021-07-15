package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithUnaryRootImpl;

public abstract class ConvertTPAddToUnaryPhysicalOp implements Rule{

    @Override
    public PhysicalPlan applyTo(PhysicalPlan pp) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) pop.getLogicalOperator();

        final UnaryPhysicalOp popUnary = definePhysicalOperator( lop );
        final PhysicalPlan ppNew = new PhysicalPlanWithUnaryRootImpl( popUnary, pp.getSubPlan(0));

        return ppNew;
    }

    @Override
    public Double getPriority() {
        return 0.2;
    }

    public abstract UnaryPhysicalOp definePhysicalOperator( final LogicalOpTPAdd lop );

}
