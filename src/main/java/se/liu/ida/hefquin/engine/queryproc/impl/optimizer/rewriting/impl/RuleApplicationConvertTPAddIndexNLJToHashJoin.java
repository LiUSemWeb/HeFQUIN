package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpHashJoin;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.cardinality.CardinalityEstimationHelper;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RewritingRule;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplicationBaseImpl;

public class RuleApplicationConvertTPAddIndexNLJToHashJoin extends RuleApplicationBaseImpl
{
    public RuleApplicationConvertTPAddIndexNLJToHashJoin( final PhysicalPlan[] currentPath, final RewritingRule rule) {
        super( currentPath, rule );
    }

    @Override
    protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator popRoot = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) popRoot.getLogicalOperator();

        final BinaryPhysicalOp popJoin = new PhysicalOpHashJoin( new LogicalOpJoin() );
        final PhysicalPlan subqueryRequest = CardinalityEstimationHelper.formRequestPlan(lop);
        return PhysicalPlanFactory.createPlan( popJoin, subqueryRequest, plan.getSubPlan(0) );
    }

}
