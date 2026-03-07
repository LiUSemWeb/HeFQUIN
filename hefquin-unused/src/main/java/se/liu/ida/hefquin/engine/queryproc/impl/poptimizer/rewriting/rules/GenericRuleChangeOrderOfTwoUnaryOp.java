package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;

public abstract class GenericRuleChangeOrderOfTwoUnaryOp extends AbstractRewritingRuleImpl{

    public GenericRuleChangeOrderOfTwoUnaryOp( final double priority ) {
        super(priority);
    }

    @Override
    protected RuleApplication createRuleApplication(final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final UnaryPhysicalOp rootOp = (UnaryPhysicalOp) plan.getRootOperator();
                final UnaryPhysicalOp subRootOp = (UnaryPhysicalOp) plan.getSubPlan(0).getRootOperator();

                final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( rootOp , plan.getSubPlan(0).getSubPlan(0) );
                return PhysicalPlanFactory.createPlan( subRootOp, newSubPlan );
            }
        };
    }

}
