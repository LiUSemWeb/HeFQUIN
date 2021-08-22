package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithUnaryRootImpl;

public class ApplyRewritingRule {

    public PhysicalPlan rewritePlan( final PhysicalPlan plan, final RewritingRule rule ) {
        final PhysicalPlan[] optSubPlans = getRewrittenSubPlans( plan, rule );
        final PhysicalPlan newPlan = constructPlan( plan, optSubPlans );

        if ( rule.canBeAppliedTo(newPlan) ){
            return rule.applyTo( newPlan );
        }
        else {
            return newPlan;
        }
    }

    protected PhysicalPlan[] getRewrittenSubPlans( final PhysicalPlan plan, final RewritingRule rule ) {
        final int numChildren = plan.numberOfSubPlans();
        final PhysicalPlan[] children = new PhysicalPlan[numChildren];
        for ( int i = 0; i < numChildren; ++i ) {
            children[i] = rewritePlan( plan.getSubPlan(i), rule );
        }
        return children;
    }

    public PhysicalPlan constructPlan( final PhysicalPlan plan, final PhysicalPlan[] optSubPlans ) {
        if ( plan.numberOfSubPlans() == 0){
            return plan;
        }
        else if ( plan.numberOfSubPlans() == 1 ){
            return new PhysicalPlanWithUnaryRootImpl((UnaryPhysicalOp) plan.getRootOperator(), optSubPlans[0]);
        }
        else if ( plan.numberOfSubPlans() == 2 ){
            return new PhysicalPlanWithBinaryRootImpl((BinaryPhysicalOp) plan.getRootOperator(), optSubPlans[0], optSubPlans[1]);
        }
        else {
            return new PhysicalPlanWithNaryRootImpl((NaryPhysicalOp) plan.getRootOperator(), optSubPlans);
        }
    }

}
