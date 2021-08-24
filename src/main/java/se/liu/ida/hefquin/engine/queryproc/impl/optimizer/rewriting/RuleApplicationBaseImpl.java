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
    protected int index;

    public RuleApplicationBaseImpl( final PhysicalPlan[] subPlans ){
        this.subPlans = subPlans;
    }

    @Override
    public PhysicalPlan getPlan() {
        return subPlans[0];
    }

    @Override
    public PhysicalPlan getResultingPlan() {
        index = 0;
        return constructPlan( subPlans[0] );
    }

    protected PhysicalPlan constructPlan( final PhysicalPlan plan ) {
        final PhysicalPlan[] newSubPlans = getRewrittenSubPlans(plan);

        if( index == subPlans.length - 1)  {
            return rewrittenSubPlan(plan);
        }
        else if ( plan.numberOfSubPlans() == 0){
            return plan;
        }
        else if ( plan.numberOfSubPlans() == 1 ){
            return new PhysicalPlanWithUnaryRootImpl((UnaryPhysicalOp) plan.getRootOperator(), newSubPlans[0]);
        }
        else if ( plan.numberOfSubPlans() == 2 ){
            return new PhysicalPlanWithBinaryRootImpl((BinaryPhysicalOp) plan.getRootOperator(), newSubPlans[0], newSubPlans[1]);
        }
        else {
            return new PhysicalPlanWithNaryRootImpl((NaryPhysicalOp) plan.getRootOperator(), newSubPlans);
        }
    }

    protected PhysicalPlan[] getRewrittenSubPlans(final PhysicalPlan plan ) {
        final int numChildren = plan.numberOfSubPlans();
        final PhysicalPlan[] children = new PhysicalPlan[numChildren];
        for (int i = 0; i< numChildren; i++) {
            index ++;
            if ( index-i < subPlans.length ) {
                children[i] = constructPlan( plan.getSubPlan(i) );
            }
        }
        return children;
    }

    protected abstract PhysicalPlan rewrittenSubPlan( final PhysicalPlan plan );

}
