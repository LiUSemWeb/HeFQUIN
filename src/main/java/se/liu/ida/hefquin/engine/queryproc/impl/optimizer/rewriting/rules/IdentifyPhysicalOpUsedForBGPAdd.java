package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class IdentifyPhysicalOpUsedForBGPAdd {

    public static boolean isIndexNLJ( final PhysicalOperator pop ) {
        return matchBGPAdd( pop ) && ( pop instanceof PhysicalOpIndexNestedLoopsJoin);
    }

    public static boolean isBindJoinFILTER( final PhysicalOperator pop ) {
        return matchBGPAdd( pop ) && ( pop instanceof PhysicalOpBindJoinWithFILTER);
    }

    public static boolean isBindJoinUNION( final PhysicalOperator pop ) {
        return matchBGPAdd( pop ) && ( pop instanceof PhysicalOpBindJoinWithUNION);
    }

    public static boolean isBindJoinVALUES( final PhysicalOperator pop ) {
        return matchBGPAdd( pop ) && ( pop instanceof PhysicalOpBindJoinWithVALUES);
    }

    protected static boolean matchBGPAdd( final PhysicalOperator pop ) {
        final PhysicalOperatorForLogicalOperator popLop = (PhysicalOperatorForLogicalOperator) pop;
        return popLop.getLogicalOperator() instanceof LogicalOpBGPAdd;
    }

}
