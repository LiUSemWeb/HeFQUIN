package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public abstract class RewritingRuleBaseImpl implements RewritingRule{
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
        return _determineAllPossibleApplications( plan, new HashSet<>(), new Stack<>() );
    }

    protected Set<RuleApplication> _determineAllPossibleApplications( final PhysicalPlan plan, final Set<RuleApplication> ruleApplications, final Stack<PhysicalPlan> currentPath ) {
        currentPath.push(plan);

        if ( canBeAppliedTo(plan) ) {
            ruleApplications.add( createRuleApplication((PhysicalPlan[]) currentPath.toArray()));
        }
        final int numChildren = plan.numberOfSubPlans();
        for (int i = 0; i < numChildren; ++i) {
            _determineAllPossibleApplications( plan.getSubPlan(i), ruleApplications, currentPath);
        }

        currentPath.pop();
        return ruleApplications;
    }

    protected abstract boolean canBeAppliedTo( final PhysicalPlan plan );

    protected abstract RuleApplication createRuleApplication( final PhysicalPlan[] currentPath );

}
