package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizationStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizerBase;
import se.liu.ida.hefquin.engine.utils.Pair;

/**
 * This class implements a simple query optimizer that focuses only
 * on join ordering, for which it uses an enumeration algorithm to
 * optimize any subplan that consists of a group of joins.
 *
 * The concrete enumeration algorithm to be used for this purpose is
 * not hard-coded but, instead, can be specified by means of providing
 * an implementation of {@link JoinPlanOptimizer}.
 */
public class SimpleJoinOrderingQueryOptimizer extends PhysicalOptimizerBase
{
    protected final JoinPlanOptimizer joinPlanOptimizer;

    public SimpleJoinOrderingQueryOptimizer( final JoinPlanOptimizer joinPlanOptimizer,
                                             final LogicalToPhysicalPlanConverter l2pConverter ) {
        super(l2pConverter);

        assert joinPlanOptimizer != null;
        this.joinPlanOptimizer = joinPlanOptimizer;
    }

    @Override
    public boolean assumesLogicalMultiwayJoins() {
        return true;
    }

    @Override
    public boolean keepMultiwayJoinsInInitialPhysicalPlan() {
        return true;
    }

    @Override
    public Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final PhysicalPlan initialPlan ) throws PhysicalOptimizationException {
        final PhysicalPlan bestPlan = optimizePlan(initialPlan);

        final PhysicalOptimizationStatsImpl myStats = new PhysicalOptimizationStatsImpl();

        return new Pair<>(bestPlan, myStats);
    }

    public PhysicalPlan optimizePlan( final PhysicalPlan plan ) throws PhysicalOptimizationException {
        if ( plan.numberOfSubPlans() == 0 ) {
            return plan;
        }

        final PhysicalPlan[] optSubPlans = getOptimizedSubPlans(plan);

        if ( hasMultiwayJoinAsRoot(plan) ){
            return joinPlanOptimizer.determineJoinPlan(optSubPlans);
        }
        else {
            return PhysicalPlanFactory.createPlan( plan.getRootOperator(), optSubPlans );
        }
    }

    protected PhysicalPlan[] getOptimizedSubPlans( final PhysicalPlan plan ) throws PhysicalOptimizationException {
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
