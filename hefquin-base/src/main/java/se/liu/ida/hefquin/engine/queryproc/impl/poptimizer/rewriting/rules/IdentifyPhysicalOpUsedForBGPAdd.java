package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class IdentifyPhysicalOpUsedForBGPAdd {

    public static boolean isIndexNLJ( final PhysicalOperator pop ) {
        return IdentifyLogicalOp.isBGPAdd( pop ) && ( pop instanceof PhysicalOpIndexNestedLoopsJoin);
    }

    public static boolean isBindJoinFILTER( final PhysicalOperator pop ) {
        return IdentifyLogicalOp.isBGPAdd( pop ) && ( pop instanceof PhysicalOpBindJoinWithFILTER);
    }

    public static boolean isBindJoinUNION( final PhysicalOperator pop ) {
        return IdentifyLogicalOp.isBGPAdd( pop ) && ( pop instanceof PhysicalOpBindJoinWithUNION);
    }

    public static boolean isBindJoinVALUES( final PhysicalOperator pop ) {
        return IdentifyLogicalOp.isBGPAdd( pop ) && ( pop instanceof PhysicalOpBindJoinWithVALUES);
    }

}
