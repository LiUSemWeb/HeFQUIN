package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class ApplyRewritingRule {

    public PhysicalPlan rewritePlan( final PhysicalPlan plan, final RewritingRule rule ) {
        final PhysicalPlan[] optSubPlans = getRewrittenSubPlans(plan, rule);

        if ( rule.canBeAppliedTo(plan) ){
            return rule.applyTo(plan, optSubPlans);
        }
        else {
            return plan;
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

}
