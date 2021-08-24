package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpHashJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RewritingRule;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplicationBaseImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.ConstructSubPPBasedOnUnaryOperator;

public class RuleApplicationConvertTPAddIndexNLJToHashJoin extends RuleApplicationBaseImpl {
    protected final ConstructSubPPBasedOnUnaryOperator helper = new ConstructSubPPBasedOnUnaryOperator();

    public RuleApplicationConvertTPAddIndexNLJToHashJoin( final PhysicalPlan[] subPlans, final RewritingRule rule) {
        super(subPlans, rule);
    }

    @Override
    protected PhysicalPlan rewrittenSubPlan( final PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator popRoot = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) popRoot.getLogicalOperator();

        final BinaryPhysicalOp popJoin = new PhysicalOpHashJoin( new LogicalOpJoin() );
        final PhysicalPlan subqueryRequest = helper.formRequestBasedOnTPofTPAdd( lop );
        return new PhysicalPlanWithBinaryRootImpl( popJoin, subqueryRequest, plan.getSubPlan(0));
    }

}
