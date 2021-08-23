package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl.RuleAppliConvertTPAddIndexNLJToHashJoin;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public abstract class RewritingRuleBaseImpl implements RewritingRule{
    protected Set<RuleApplication> ruleApplications = new HashSet<>();
    protected Stack<PhysicalPlan> subPlans = new Stack<>();
    protected double priority;

    public RewritingRuleBaseImpl( final double priority ) {
        this.priority = priority;
    }

    @Override
    public double getPriority() {
        return priority;
    }

    @Override
    public Set<RuleApplication> determineAllPossibleApplications( final PhysicalPlan plan ) {
        subPlans.push(plan);

        if ( canBeAppliedTo(plan) ) {
            ruleApplications.add( new RuleAppliConvertTPAddIndexNLJToHashJoin((PhysicalPlan[]) subPlans.toArray()) );
        }

        final int numChildren = plan.numberOfSubPlans();
        for (int i = 0; i < numChildren; ++i) {
            determineAllPossibleApplications( plan.getSubPlan(i) );
        }
        return ruleApplications;
    }

    protected abstract boolean canBeAppliedTo( final PhysicalPlan plan );

}
