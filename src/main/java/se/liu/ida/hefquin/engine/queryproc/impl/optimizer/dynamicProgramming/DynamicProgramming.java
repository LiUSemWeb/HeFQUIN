package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.dynamicProgramming;

import se.liu.ida.hefquin.engine.federation.FederationAccessException;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.*;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

import java.util.*;

public class DynamicProgramming extends CardinalityEstimation {
    protected final QueryProcContext ctxt;
    protected final List<PhysicalPlan> lpList;

    public DynamicProgramming(final QueryProcContext ctxt, final List<PhysicalPlan> lpList) {
        assert ctxt != null;
        this.ctxt = ctxt;
        this.lpList = lpList;
    }

    protected PhysicalPlan optimizePhysicalPlanForMultiwayJoin() throws FederationAccessException {
        PhysicalPlan currentPlan = chooseFirstSubquery();

        while (lpList.size() > 0){
            currentPlan = cardinalityTwoSubQueries(currentPlan);
        }

        return currentPlan;
    }

    public PhysicalPlan chooseFirstSubquery() throws FederationAccessException {
        PhysicalPlan firstPlan = lpList.get(0);
        int initialCost = getCardinalityEstimationOfLeafNode(firstPlan, ctxt.getFederationAccessMgr());;

        for ( int i = 1; i < lpList.size(); ++i ){
            final PhysicalPlan nextPlan = lpList.get(i);
            final int cost = getCardinalityEstimationOfLeafNode(nextPlan, ctxt.getFederationAccessMgr());
            if (cost < initialCost){
                firstPlan = nextPlan;
            }
            initialCost = cost;
        }

        lpList.remove(firstPlan);
        return firstPlan;
    }

    public PhysicalPlan cardinalityTwoSubQueries(final PhysicalPlan currentPlan) throws FederationAccessException {
        PhysicalPlan nextPlan = lpList.get(0);
        final PhysicalPlan lp = new PhysicalPlanWithBinaryRootImpl(convertJoin( new LogicalOpJoin() ), currentPlan, nextPlan);
        int initialCost = getJoinCardinalityEstimation(lp, ctxt.getFederationAccessMgr());

        for ( int i = 1; i < lpList.size(); ++i ){
            final PhysicalPlan candidatePlan = lpList.get(i);
            final PhysicalPlan lpC = new PhysicalPlanWithBinaryRootImpl( convertJoin( new LogicalOpJoin() ), currentPlan, candidatePlan);

            final int cost = getJoinCardinalityEstimation(lpC, ctxt.getFederationAccessMgr());
            if (cost < initialCost){
                nextPlan = candidatePlan;
            }
            initialCost = cost;

        }
        final PhysicalPlan ppNew = new PhysicalPlanWithBinaryRootImpl( convertJoin( new LogicalOpJoin() ), currentPlan, nextPlan);
        return ppNew;
    }

    protected BinaryPhysicalOp convertJoin( final LogicalOpJoin lop ) {
        return new PhysicalOpSymmetricHashJoin(lop);
    }
}
