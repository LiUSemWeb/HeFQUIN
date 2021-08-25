package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithUnaryRootImpl;

public abstract class RuleApplicationBaseImpl implements RuleApplication{
    protected final PhysicalPlan[] subPlans;
    protected RewritingRule rule;

    public RuleApplicationBaseImpl( final PhysicalPlan[] subPlans, final RewritingRule rule ){
        this.subPlans = subPlans;
        this.rule = rule;
    }

    @Override
    public PhysicalPlan getPlan() {
        return subPlans[0];
    }

    @Override
    public RewritingRule getRule() {
        return rule;
    }

    @Override
    public PhysicalPlan getResultingPlan() {
        final int numStep = subPlans.length;
        PhysicalPlan rewrittenPlan = rewrittenSubPlan( subPlans[numStep-1] );

        for (int i = numStep-2; i >= 0; i--) {
            rewrittenPlan = constructPlan( subPlans[i], rewrittenPlan, i );
        }
        return rewrittenPlan;
    }

    protected PhysicalPlan constructPlan( final PhysicalPlan parent, final PhysicalPlan rewrittenChild, final int indexOfPath) {

        if ( parent.numberOfSubPlans() == 0){
            return parent;
        }
        else if ( parent.numberOfSubPlans() == 1 ){
            return new PhysicalPlanWithUnaryRootImpl((UnaryPhysicalOp) parent.getRootOperator(), rewrittenChild);
        }
        else if ( parent.numberOfSubPlans() == 2 ){
            final PhysicalPlan[] newSubPlans = getRewrittenSubPlans( parent, rewrittenChild, indexOfPath );
            return new PhysicalPlanWithBinaryRootImpl((BinaryPhysicalOp) parent.getRootOperator(), newSubPlans[0], newSubPlans[1]);
        }
        else {
            PhysicalPlan[] newSubPlans = getRewrittenSubPlans( parent, rewrittenChild, indexOfPath );
            return new PhysicalPlanWithNaryRootImpl((NaryPhysicalOp) parent.getRootOperator(), newSubPlans);
        }
    }

    protected PhysicalPlan[] getRewrittenSubPlans( final PhysicalPlan parent, final PhysicalPlan rewrittenChild, final int indexOfPath) {
        final int numChildren = parent.numberOfSubPlans();
        final PhysicalPlan[] children = new PhysicalPlan[numChildren];
        for (int i = 0; i< numChildren; i++) {
            if ( parent.getSubPlan(0) == subPlans[indexOfPath+1]) {
                children[i] = rewrittenChild;
            }
            else {
                children[i] = parent.getSubPlan(i);
            }
        }
        return children;
    }

    protected abstract PhysicalPlan rewrittenSubPlan( final PhysicalPlan plan );

}
