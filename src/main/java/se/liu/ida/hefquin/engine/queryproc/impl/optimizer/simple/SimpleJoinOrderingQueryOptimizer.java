package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;

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

        final PhysicalPlan finalPlan = optimizePlan( initialPhysicalPlan );
        ctxt.getMetrics().putCost(  CostEstimationUtils.getEstimates( ctxt.getCostModel(), finalPlan )[0] );

        return finalPlan;
    }

    public PhysicalPlan optimizePlan( final PhysicalPlan plan ) throws QueryOptimizationException {
        final PhysicalPlan[] optSubPlans = getOptimizedSubPlans(plan);

        if ( hasMultiwayJoinAsRoot(plan) ){
            return joinPlanOptimizer.determineJoinPlan(optSubPlans);
        }
        else if ( plan.numberOfSubPlans() == 0){
            return plan;
        }
        else {
            return PhysicalPlanFactory.createPlan( plan.getRootOperator(), optSubPlans );
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

}
