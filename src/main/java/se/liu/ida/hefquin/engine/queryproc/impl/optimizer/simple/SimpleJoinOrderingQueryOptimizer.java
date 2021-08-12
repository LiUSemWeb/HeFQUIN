package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;

public class SimpleJoinOrderingQueryOptimizer implements QueryOptimizer
{
    protected final QueryOptimizationContext ctxt;

    public SimpleJoinOrderingQueryOptimizer( final QueryOptimizationContext ctxt ) {
        assert ctxt != null;
        this.ctxt = ctxt;
    }

    @Override
    public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
        final boolean keepMultiwayJoins = true;
        final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert( initialPlan, keepMultiwayJoins );

        return optimizePlan( initialPhysicalPlan );
    }

    public PhysicalPlan optimizePlan( final PhysicalPlan plan ) throws QueryOptimizationException {
        if ( hasMultiwayJoinAsRoot(plan) ){
            final PhysicalPlan[] subplans = getOptimizedSubPlans(plan);
            return determinePlanToJoinSubPlans( subplans );
        }
        else {
        	// TODO incorrect!! There may be multiway joins inside the subplans of the given plan.
            return plan;
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

    public PhysicalPlan determinePlanToJoinSubPlans( final PhysicalPlan[] subplans ) throws QueryOptimizationException {
        if ( subplans.length == 1 ){
            return subplans[0];
        } else if ( subplans.length > 1 ){
            final GreedyEnumeration dp = new GreedyEnumeration( ctxt, subplans );
            return dp.getResultingPlan();
        } else {
            throw new IllegalArgumentException( "unexpected number of sub-plans: " + subplans.length );
        }
    }

    protected boolean hasMultiwayJoinAsRoot( final PhysicalPlan plan ) {
        final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
        return rootOp instanceof LogicalOpMultiwayJoin;
    }

}
