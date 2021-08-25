package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

/**
 * This class contains methods that identifying the root physical operator of a physical plan.
 * It helps to check whether the root physical operator a physical plan can be applied to a rule.
 */
public class IdentifyPhysicalOperatorOfTPAdd {

    public static boolean matchTPAddIndexNLJ( final PhysicalOperator pop ) {
        return matchTPAdd( pop ) && ( pop instanceof PhysicalOpIndexNestedLoopsJoin );
    }

    public static boolean matchTPAddBJFILTER( final PhysicalOperator pop ) {
        return matchTPAdd( pop ) && ( pop instanceof PhysicalOpBindJoinWithFILTER);
    }

    public static boolean matchTPAddBJUNION( final PhysicalOperator pop ) {
        return matchTPAdd( pop ) && ( pop instanceof PhysicalOpBindJoinWithUNION);
    }

    public static boolean matchTPAddBJVALUES( final PhysicalOperator pop ) {
        return matchTPAdd( pop ) && ( pop instanceof PhysicalOpBindJoinWithVALUES);
    }

    public static boolean matchTPAddBindJoin( final PhysicalOperator pop ) {
        return matchTPAdd( pop ) && ( pop instanceof PhysicalOpBindJoin);
    }

    protected static boolean matchTPAdd(final PhysicalOperator pop) {
        final PhysicalOperatorForLogicalOperator popLop = (PhysicalOperatorForLogicalOperator)pop;
        return popLop.getLogicalOperator() instanceof LogicalOpTPAdd;
    }

}
