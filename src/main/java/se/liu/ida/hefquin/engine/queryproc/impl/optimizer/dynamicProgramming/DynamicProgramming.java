package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.dynamicProgramming;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.*;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

import java.util.*;

public class DynamicProgramming {
    protected final QueryProcContext ctxt;
    protected final List<PhysicalPlan> lpList;
    protected final CardinalityEstimation cardEstimate;

    public DynamicProgramming( final QueryProcContext ctxt, final List<PhysicalPlan> lpList ) {
        assert ctxt != null;
        assert lpList.size() > 0;
        this.ctxt = ctxt;
        this.lpList = lpList;
        this.cardEstimate = new CardinalityEstimation(ctxt);
    }

    public PhysicalPlan optimizePhysicalPlanForMultiwayJoin() throws QueryOptimizationException {
        PhysicalPlan currentPlan = chooseFirstSubquery();

        while ( lpList.size() > 0 ){
            currentPlan = cardinalityTwoSubQueries(currentPlan);
        }

        return currentPlan;
    }

    protected PhysicalPlan chooseFirstSubquery() throws QueryOptimizationException {
        PhysicalPlan firstPlan = lpList.get(0);
        int initialCost = cardEstimate.getCardinalityEstimationOfLeafNode(firstPlan );;

        for ( int i = 1; i < lpList.size(); ++i ){
            final PhysicalPlan nextPlan = lpList.get(i);
            final int cost = cardEstimate.getCardinalityEstimationOfLeafNode( nextPlan );
            if ( cost < initialCost ){
                firstPlan = nextPlan;
                initialCost = cost;
            }
        }

        lpList.remove( firstPlan );
        return firstPlan;
    }

    protected PhysicalPlan cardinalityTwoSubQueries( final PhysicalPlan currentPlan ) throws QueryOptimizationException {
        PhysicalPlan newPlan = new PhysicalPlanWithBinaryRootImpl(convertJoin( new LogicalOpJoin() ), currentPlan, lpList.get(0) );
        int initialCost = cardEstimate.getJoinCardinalityEstimation(newPlan );

        for ( int i = 1; i < lpList.size(); ++i ){
            final PhysicalPlan lpCandidate = new PhysicalPlanWithBinaryRootImpl( convertJoin( new LogicalOpJoin() ), currentPlan, lpList.get(i) );

            final int cost = cardEstimate.getJoinCardinalityEstimation( lpCandidate );
            if ( cost < initialCost ){
                newPlan = lpCandidate;
                initialCost = cost;
            }
        }
        return newPlan;
    }

    protected BinaryPhysicalOp convertJoin( final LogicalOpJoin lop ) {
        return new PhysicalOpSymmetricHashJoin(lop);
    }

}
