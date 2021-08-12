package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.dynamicProgramming;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpSymmetricHashJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimationImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimationUtils;

public class DynamicProgramming {
    protected final QueryProcContext ctxt;
    protected final List<PhysicalPlan> lpList;
    protected final CardinalityEstimation cardEstimate;

    public DynamicProgramming( final QueryProcContext ctxt, final List<PhysicalPlan> lpList ) {
        assert ctxt != null;
        assert lpList.size() > 0;
        this.ctxt = ctxt;
        this.lpList = lpList;
        this.cardEstimate = new CardinalityEstimationImpl(ctxt);
    }

    public PhysicalPlan optimizePhysicalPlanForMultiwayJoin() throws QueryOptimizationException {
        PhysicalPlan currentPlan = chooseFirstSubquery();

        while ( lpList.size() > 0 ){
            currentPlan = cardinalityTwoSubQueries(currentPlan);
        }

        return currentPlan;
    }

    protected PhysicalPlan chooseFirstSubquery() throws QueryOptimizationException {
        final int[] costs = CardinalityEstimationUtils.getEstimates(cardEstimate, lpList);

        PhysicalPlan bestPlan = lpList.get(0);
        int costOfBestPlan = costs[0];

        for ( int i = 1; i < lpList.size(); ++i ){
            if ( costOfBestPlan > costs[i] ){
            	bestPlan = lpList.get(i);
                costOfBestPlan = costs[i];
            }
        }

        lpList.remove(bestPlan);
        return bestPlan;
    }

    protected PhysicalPlan cardinalityTwoSubQueries( final PhysicalPlan currentPlan )
            throws QueryOptimizationException
    {
        final PhysicalPlan[] nextPossiblePlans = createNextPossiblePlans(currentPlan);
        final int[] costs = CardinalityEstimationUtils.getEstimates(cardEstimate, nextPossiblePlans);

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
