package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpIndexNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public abstract class GenericRuleConvertTPAddToSymmetricHashJoin extends AbstractRewritingRuleImpl{

    public GenericRuleConvertTPAddToSymmetricHashJoin( final double priority ) {
        super(priority);
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOpIndexNestedLoopsJoin rootOp = (PhysicalOpIndexNestedLoopsJoin) plan.getRootOperator();
                final PhysicalPlan reqPlan = PhysicalPlanFactory.extractRequestAsPlan(rootOp);
                return PhysicalPlanFactory.createPlanWithSymmetricHashJoin( reqPlan, plan.getSubPlan(0) );
            }
        };
    }

}
