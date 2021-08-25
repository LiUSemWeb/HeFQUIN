package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpIndexNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

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
                final PhysicalOpIndexNestedLoopsJoin rootOp = (PhysicalOpIndexNestedLoopsJoin) plan.getRootOperator();
                final PhysicalPlan reqPlan = PhysicalPlanFactory.extractRequestAsPlan(rootOp);
                return PhysicalPlanFactory.createPlanWithHashJoin( reqPlan, plan.getSubPlan(0) );
            }
        };
    }

}
