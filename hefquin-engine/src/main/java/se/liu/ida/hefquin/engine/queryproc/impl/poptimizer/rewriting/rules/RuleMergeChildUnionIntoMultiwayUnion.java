package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;

import java.util.ArrayList;
import java.util.List;

public class RuleMergeChildUnionIntoMultiwayUnion extends AbstractRewritingRuleImpl{

    public RuleMergeChildUnionIntoMultiwayUnion( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator op = plan.getRootOperator();
        if ( IdentifyLogicalOp.isMultiwayUnion(op) ) {
            for ( int i = 0; i < plan.numberOfSubPlans(); i ++ ) {
                final PhysicalOperator subOp = plan.getSubPlan(i).getRootOperator();
                if ( IdentifyLogicalOp.isUnion(subOp) ) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final List<PhysicalPlan> subPlans = new ArrayList<>();
                for ( int i = 0; i < plan.numberOfSubPlans(); i++) {
                    final PhysicalPlan subPlan = plan.getSubPlan(i);
                    if ( IdentifyLogicalOp.isUnion(subPlan.getRootOperator()) ) {
                        subPlans.add( subPlan.getSubPlan(0) );
                        subPlans.add( subPlan.getSubPlan(1) );
                    }
                    else {
                        subPlans.add( subPlan );
                    }
                }

                return PhysicalPlanFactory.createPlan( (NaryLogicalOp) plan.getRootOperator(), subPlans );
            }
        };
    }

}
