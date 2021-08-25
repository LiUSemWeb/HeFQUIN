package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RewritingRule;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public abstract class RuleApplicationBaseImpl implements RuleApplication
{
    protected final PhysicalPlan[] pathToTargetSubPlan;
    protected final RewritingRule rule;

    public RuleApplicationBaseImpl( final PhysicalPlan[] pathToTargetSubPlan,
                                    final RewritingRule rule ) {
        assert pathToTargetSubPlan.length > 0;
        assert rule != null;

        this.pathToTargetSubPlan = pathToTargetSubPlan;
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

        // first, rewrite the target (sub)plan according to this rewriting rule
        PhysicalPlan rewrittenPlan = rewritePlan( pathToTargetSubPlan[numSteps] );

        // next, reconstruct the ancestors all the way up to the root of the original plan
        for ( int i = numSteps-1; i >= 0; i-- ) {
            final PhysicalPlan parent = pathToTargetSubPlan[i];
            final PhysicalPlan rewrittenChildBefore = pathToTargetSubPlan[i+1];
            final PhysicalPlan rewrittenChildAfter = rewrittenPlan;
            rewrittenPlan = reconstructParentPlan(parent, rewrittenChildBefore, rewrittenChildAfter);
        }

        return rewrittenPlan;
    }

    protected PhysicalPlan reconstructParentPlan( final PhysicalPlan parent,
                                                  final PhysicalPlan rewrittenChildBefore,
                                                  final PhysicalPlan rewrittenChildAfter ) {
        if ( parent.numberOfSubPlans() == 0 ) {
            // this case should never occur
            throw new IllegalArgumentException();
        }
        else if ( parent.numberOfSubPlans() == 1 ) {
            final UnaryPhysicalOp rootOp = (UnaryPhysicalOp) parent.getRootOperator();
            return PhysicalPlanFactory.createPlan(rootOp, rewrittenChildAfter);
        }
        else if ( parent.numberOfSubPlans() == 2 ){
            final PhysicalPlan[] newSubPlans = getNewChildPlans( parent, rewrittenChildBefore, rewrittenChildAfter );
            final BinaryPhysicalOp rootOp = (BinaryPhysicalOp) parent.getRootOperator();
            return PhysicalPlanFactory.createPlan(rootOp, newSubPlans[0], newSubPlans[1]);
        }
        else {
            final PhysicalPlan[] newSubPlans = getNewChildPlans( parent, rewrittenChildBefore, rewrittenChildAfter );
            final NaryPhysicalOp rootOp = (NaryPhysicalOp) parent.getRootOperator();
            return PhysicalPlanFactory.createPlan(rootOp, newSubPlans);
        }
    }

    protected PhysicalPlan[] getNewChildPlans( final PhysicalPlan parent,
                                               final PhysicalPlan rewrittenChildBefore,
                                               final PhysicalPlan rewrittenChildAfter ) {
        final int numChildren = parent.numberOfSubPlans();
        final PhysicalPlan[] children = new PhysicalPlan[numChildren];
        for ( int i = 0; i < numChildren; i++ ) {
            final PhysicalPlan ithOriginalChild = parent.getSubPlan(i);
            if ( ithOriginalChild == rewrittenChildBefore ) {
                children[i] = rewrittenChildAfter;
            }
            else {
                children[i] = ithOriginalChild;
            }
        }
        return children;
    }

    protected abstract PhysicalPlan rewritePlan( final PhysicalPlan plan );

}
