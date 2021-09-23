package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleDistributeJOINOverUNION extends AbstractRewritingRuleImpl{

    public RuleDistributeJOINOverUNION( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        // root operator is join, and one of the sub plans has union as root
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.matchJoin(rootOp) ) {
            return ( IdentifyLogicalOp.matchUnion( plan.getSubPlan(0).getRootOperator() )
                    || IdentifyLogicalOp.matchUnion( plan.getSubPlan(1).getRootOperator() ) );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final BinaryPhysicalOp rootOp = (BinaryPhysicalOp) plan.getRootOperator();

                if ( plan.getSubPlan(0).getRootOperator() instanceof PhysicalOpBinaryUnion ) {
                    final PhysicalPlan newSubPlan1 = PhysicalPlanFactory.createPlan( rootOp, plan.getSubPlan(0).getSubPlan(0), plan.getSubPlan(1) );
                    final PhysicalPlan newSubPlan2 = PhysicalPlanFactory.createPlan( rootOp, plan.getSubPlan(0).getSubPlan(1), plan.getSubPlan(1) );

                    return PhysicalPlanFactory.createPlan( plan.getSubPlan(0).getRootOperator(), newSubPlan1, newSubPlan2 );
                }
                else if ( plan.getSubPlan(1).getRootOperator() instanceof PhysicalOpBinaryUnion ) {
                    final PhysicalPlan newSubPlan1 = PhysicalPlanFactory.createPlan( rootOp, plan.getSubPlan(0), plan.getSubPlan(1).getSubPlan(0) );
                    final PhysicalPlan newSubPlan2 = PhysicalPlanFactory.createPlan( rootOp, plan.getSubPlan(0), plan.getSubPlan(1).getSubPlan(1) );

                    return PhysicalPlanFactory.createPlan( plan.getSubPlan(1).getRootOperator(), newSubPlan1, newSubPlan2 );
                }
                else  {
                    return plan;
                }
            }
        };
    }

}
