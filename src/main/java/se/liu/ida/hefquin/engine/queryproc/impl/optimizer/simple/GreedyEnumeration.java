package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpSymmetricHashJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;

public class GreedyEnumeration {
    protected final List<PhysicalPlan> lpList;
    protected final CostModel costModel;

    public GreedyEnumeration( final QueryOptimizationContext ctxt, final PhysicalPlan[] subplans ) {
        assert ctxt != null;
        assert subplans.length > 0;

        this.lpList = Arrays.asList(subplans);
        this.costModel = ctxt.getCostModel();
    }

    public PhysicalPlan getResultingPlan() throws QueryOptimizationException {
        PhysicalPlan currentPlan = chooseFirstSubquery();

        while ( lpList.size() > 0 ){
            currentPlan = cardinalityTwoSubQueries(currentPlan);
        }

        return currentPlan;
    }

    protected PhysicalPlan chooseFirstSubquery() throws QueryOptimizationException {
        final double[] costs = CostEstimationUtils.getEstimates(costModel, lpList);

        int indexOfBestPlan = 0;

        for ( int i = 1; i < lpList.size(); ++i ){
            if ( costs[indexOfBestPlan] > costs[i] ){
            	indexOfBestPlan = i;
            }
        }

        final PhysicalPlan bestPlan = lpList.get(indexOfBestPlan);
        lpList.remove(indexOfBestPlan);
        return bestPlan;
    }

    protected PhysicalPlan cardinalityTwoSubQueries( final PhysicalPlan currentPlan )
            throws QueryOptimizationException
    {
        final PhysicalPlan[] nextPossiblePlans = createNextPossiblePlans(currentPlan);
        final double[] costs = CostEstimationUtils.getEstimates(costModel, nextPossiblePlans);

        int indexOfBestPlan = 0;

        for ( int i = 1; i < lpList.size(); ++i ){
            if ( costs[indexOfBestPlan] > costs[i] ){
                indexOfBestPlan = i;
            }
        }

        lpList.remove(indexOfBestPlan);
        return nextPossiblePlans[indexOfBestPlan];
    }

    protected PhysicalPlan[] createNextPossiblePlans( final PhysicalPlan currentPlan ) {
        final PhysicalPlan[] plans = new PhysicalPlan[ lpList.size() ];
        for ( int i = 0; i < lpList.size(); ++i ) {
            final BinaryPhysicalOp joinOp = createNewJoinOperator();
            plans[i] = new PhysicalPlanWithBinaryRootImpl( joinOp, currentPlan, lpList.get(i) );
        }
        return plans;
    }

    protected BinaryPhysicalOp createNewJoinOperator() {
        return new PhysicalOpSymmetricHashJoin( new LogicalOpJoin() );
    }

}
