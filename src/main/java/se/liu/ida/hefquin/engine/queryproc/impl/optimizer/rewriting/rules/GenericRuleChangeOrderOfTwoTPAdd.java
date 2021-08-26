package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public abstract class GenericRuleChangeOrderOfTwoTPAdd extends AbstractRewritingRuleImpl{

    public GenericRuleChangeOrderOfTwoTPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected RuleApplication createRuleApplication(final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperator rootOp = plan.getRootOperator();
                final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();

                final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( rootOp , plan.getSubPlan(0).getSubPlan(0) );
                return PhysicalPlanFactory.createPlan( subRootOp, newSubPlan );
            }
        };
    }

}
