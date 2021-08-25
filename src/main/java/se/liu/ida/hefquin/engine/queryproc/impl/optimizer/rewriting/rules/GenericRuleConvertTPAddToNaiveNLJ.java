package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public abstract class GenericRuleConvertTPAddToNaiveNLJ extends AbstractRewritingRuleImpl{

    public GenericRuleConvertTPAddToNaiveNLJ( final double priority ) {
        super(priority);
    }

    @Override
    protected RuleApplication createRuleApplication(final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
                final PhysicalPlan reqPlan = PhysicalPlanFactory.extractRequestAsPlan((LogicalOpTPAdd) rootOp.getLogicalOperator());
                return PhysicalPlanFactory.createPlanWithNaiveNLJ( reqPlan, plan.getSubPlan(0) );
            }
        };
    }

}
