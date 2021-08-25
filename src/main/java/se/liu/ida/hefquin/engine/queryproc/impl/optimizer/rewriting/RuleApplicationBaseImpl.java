package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithUnaryRootImpl;

public abstract class RuleApplicationBaseImpl implements RuleApplication{
    protected final PhysicalPlan[] pathToTargetSubPlan;
    protected RewritingRule rule;

    public RuleApplicationBaseImpl( final PhysicalPlan[] currentPath, final RewritingRule rule ) {
        assert currentPath.length > 0;
        assert rule != null;
        this.pathToTargetSubPlan = currentPath;
        this.rule = rule;
    }

    @Override
    public PhysicalPlan getPlan() {
        return pathToTargetSubPlan[0];
    }

    @Override
    public RewritingRule getRule() {
        return rule;
    }

    @Override
    public PhysicalPlan getResultingPlan() {
        final int numSteps = pathToTargetSubPlan.length - 1;
        PhysicalPlan rewrittenPlan = rewritePlan( pathToTargetSubPlan[numSteps] );

        for (int i = numSteps-1; i >= 0; i--) {
            rewrittenPlan = constructPlan( pathToTargetSubPlan[i], pathToTargetSubPlan[i+1], rewrittenPlan );
        }
        return rewrittenPlan;
    }

    protected PhysicalPlan constructPlan( final PhysicalPlan parent, final PhysicalPlan originalChild, final PhysicalPlan rewrittenChild ) {

        if ( parent.numberOfSubPlans() == 0){
            throw new IllegalArgumentException(" Unexpected parent operator");
        }
        else if ( parent.numberOfSubPlans() == 1 ){
            return new PhysicalPlanWithUnaryRootImpl( (UnaryPhysicalOp) parent.getRootOperator(), rewrittenChild );
        }
        else if ( parent.numberOfSubPlans() == 2 ){
            final PhysicalPlan[] newSubPlans = getRewrittenSubPlans( parent, originalChild, rewrittenChild );
            return new PhysicalPlanWithBinaryRootImpl( (BinaryPhysicalOp) parent.getRootOperator(), newSubPlans[0], newSubPlans[1] );
        }
        else {
            final PhysicalPlan[] newSubPlans = getRewrittenSubPlans( parent, originalChild, rewrittenChild );
            return new PhysicalPlanWithNaryRootImpl( (NaryPhysicalOp) parent.getRootOperator(), newSubPlans );
        }
    }

    protected PhysicalPlan[] getRewrittenSubPlans( final PhysicalPlan parent, final PhysicalPlan originalChild, final PhysicalPlan rewrittenChild ) {
        final int numChildren = parent.numberOfSubPlans();
        final PhysicalPlan[] children = new PhysicalPlan[numChildren];
        for (int i = 0; i< numChildren; i++) {
            if ( parent.getSubPlan(i) == originalChild) {
                children[i] = rewrittenChild;
            }
            else {
                children[i] = parent.getSubPlan(i);
            }
        }
        return children;
    }

    protected abstract PhysicalPlan rewritePlan( final PhysicalPlan plan );

}
