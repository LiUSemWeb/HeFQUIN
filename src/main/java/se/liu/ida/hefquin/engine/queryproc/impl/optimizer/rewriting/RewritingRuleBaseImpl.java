package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public abstract class RewritingRuleBaseImpl implements RewritingRule
{
    protected final double priority;

    public RewritingRuleBaseImpl( final double priority ) {
        assert priority >= 0;
        this.priority = priority;
    }

    @Override
    public double getPriority() {
        return priority;
    }

    @Override
    public Set<RuleApplication> determineAllPossibleApplications( final PhysicalPlan plan ) {
        final Set<RuleApplication> collectedRuleApps = new HashSet<>();
        collectAllPossibleApplications( plan, collectedRuleApps, new Stack<>() );
        return collectedRuleApps;
    }

    protected void collectAllPossibleApplications( final PhysicalPlan plan,
                                                   final Set<RuleApplication> collectedRuleApps,
                                                   final Stack<PhysicalPlan> currentPathFromRoot ) {
        currentPathFromRoot.push(plan);

        if ( canBeAppliedTo(plan) ) {
            final PhysicalPlan[] pathToTargetPlan = (PhysicalPlan[]) currentPathFromRoot.toArray();
            final RuleApplication app = createRuleApplication(pathToTargetPlan);
            collectedRuleApps.add(app);
        }

        // recursion to check also within all subplans
        final int numChildren = plan.numberOfSubPlans();
        for ( int i = 0; i < numChildren; ++i ) {
            collectAllPossibleApplications( plan.getSubPlan(i),
                                               collectedRuleApps,
                                               currentPathFromRoot );
        }

        currentPathFromRoot.pop();
    }

    protected abstract boolean canBeAppliedTo( final PhysicalPlan plan );

    protected abstract RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan );

}
