package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.cardinality.CardinalityEstimationHelper;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.IdentifyPhysicalOperatorOfTPAdd;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RewritingRuleBaseImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplicationBaseImpl;

/**
 * This is a rewriting rule that convert a TPAdd(indexNLJ) operator to Hash Join.
 */
public class RuleConvertTPAddIndexNLJToHashJoin extends RewritingRuleBaseImpl
{
    public RuleConvertTPAddIndexNLJToHashJoin( final double priority ){
        super(priority);
    }

    @Override
    public boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        return IdentifyPhysicalOperatorOfTPAdd.matchTPAddIndexNLJ(rootOp);
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan )
    {
        return new RuleApplicationBaseImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperatorForLogicalOperator popRoot = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
                final LogicalOpTPAdd lop = (LogicalOpTPAdd) popRoot.getLogicalOperator();
                final PhysicalPlan subqueryRequest = CardinalityEstimationHelper.formRequestPlan(lop);
                return PhysicalPlanFactory.createPlanWithHashJoin( subqueryRequest, plan.getSubPlan(0) );
            }
        };
    }

}
