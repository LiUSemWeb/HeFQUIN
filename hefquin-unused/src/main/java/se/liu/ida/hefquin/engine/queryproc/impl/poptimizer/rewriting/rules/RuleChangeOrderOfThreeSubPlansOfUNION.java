package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;

public class RuleChangeOrderOfThreeSubPlansOfUNION extends AbstractRewritingRuleImpl{

    public RuleChangeOrderOfThreeSubPlansOfUNION( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.isUnion(rootOp) ) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            return IdentifyLogicalOp.isUnion( subPlanOp1 ) || IdentifyLogicalOp.isUnion( subPlanOp2 );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final BinaryPhysicalOp rootOp = (BinaryPhysicalOp) plan.getRootOperator();

                final PhysicalPlan subPlan1 = plan.getSubPlan(0);
                final PhysicalPlan subPlan2 = plan.getSubPlan(1);
                final PhysicalOperator subPlanOp1 = subPlan1.getRootOperator();
                final PhysicalOperator subPlanOp2 = subPlan2.getRootOperator();

                if ( IdentifyLogicalOp.isUnion( subPlanOp1 ) ) {
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( subPlanOp1, subPlan1.getSubPlan(1), subPlan2 );
                    return PhysicalPlanFactory.createPlan( rootOp, subPlan1.getSubPlan(0), newSubPlan );
                }
                else if ( IdentifyLogicalOp.isUnion( subPlanOp2 ) ) {
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( subPlanOp2, subPlan1, subPlan2.getSubPlan(0));
                    return PhysicalPlanFactory.createPlan( rootOp, newSubPlan, subPlan2.getSubPlan(1) );
                }
                else  {
                    return plan;
                }
            }
        };
    }

}
