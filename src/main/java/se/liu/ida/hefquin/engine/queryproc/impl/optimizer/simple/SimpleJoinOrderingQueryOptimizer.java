package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;

/**
 * This class implements a simple query optimizer that focuses only
 * on join ordering, for which it uses an enumeration algorithm to
 * optimize any subplan that consists of a group of joins.
 *
 * The concrete enumeration algorithm to be used for this purpose is
 * not hard-coded but, instead, can be specified by means of providing
 * an implementation of {@link JoinPlanOptimizer}.
 */
public class SimpleJoinOrderingQueryOptimizer implements QueryOptimizer
{
	protected final JoinPlanOptimizer joinPlanOptimizer;
    protected final QueryOptimizationContext ctxt;

    public SimpleJoinOrderingQueryOptimizer( final JoinPlanOptimizer joinPlanOptimizer,
                                             final QueryOptimizationContext ctxt ) {
        assert joinPlanOptimizer != null;
        assert ctxt != null;

        this.joinPlanOptimizer = joinPlanOptimizer;
        this.ctxt = ctxt;
    }

    @Override
    public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
        final boolean keepMultiwayJoins = true;
        final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert( initialPlan, keepMultiwayJoins );

        return optimizePlan( initialPhysicalPlan );
    }

    public PhysicalPlan optimizePlan( final PhysicalPlan plan ) throws QueryOptimizationException {
        final PhysicalPlan[] optSubPlans = getOptimizedSubPlans(plan);

        if ( hasMultiwayJoinAsRoot(plan) ){
            return joinPlanOptimizer.determineJoinPlan(optSubPlans);
        }
        else if (hasNullaryOpAsRoot(plan)){
            return plan;
        }
        else if ( hasUnaryOpAsRoot(plan) ){
            final PhysicalPlan newChild = optimizePlan(optSubPlans[0]);
            return new PhysicalPlanWithUnaryRootImpl((UnaryPhysicalOp) plan.getRootOperator(), newChild);
        }
        else if ( hasBinaryOpAsRoot(plan) ){
            final PhysicalPlan newChild1 = optimizePlan(optSubPlans[0]);
            final PhysicalPlan newChild2 = optimizePlan(optSubPlans[1]);
            return new PhysicalPlanWithBinaryRootImpl((BinaryPhysicalOp) plan.getRootOperator(), newChild1, newChild2);
        }
        else {
            throw new IllegalArgumentException( "unknown root operator: " + plan.getRootOperator().getClass().getName() );
        }
    }

    protected PhysicalPlan[] getOptimizedSubPlans( final PhysicalPlan plan ) throws QueryOptimizationException {
        final int numChildren = plan.numberOfSubPlans();
        final PhysicalPlan[] children = new PhysicalPlan[numChildren];
        for ( int i = 0; i < numChildren; ++i ) {
            children[i] = optimizePlan( plan.getSubPlan(i) );
        }
        return children;
    }

    protected boolean hasMultiwayJoinAsRoot( final PhysicalPlan plan ) {
        final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
        return rootOp instanceof LogicalOpMultiwayJoin;
    }

    protected boolean hasNullaryOpAsRoot( final PhysicalPlan plan ) {
        final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
        return rootOp instanceof NullaryLogicalOp;
    }

    protected boolean hasUnaryOpAsRoot( final PhysicalPlan plan ) {
        final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
        return rootOp instanceof UnaryLogicalOp;
    }

    protected boolean hasBinaryOpAsRoot( final PhysicalPlan plan ) {
        final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
        return rootOp instanceof BinaryLogicalOp;
    }

}
