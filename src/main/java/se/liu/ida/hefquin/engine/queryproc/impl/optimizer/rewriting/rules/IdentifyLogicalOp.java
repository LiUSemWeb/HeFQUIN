package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;

public class IdentifyLogicalOp {

    public static boolean matchJoin( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        return lop instanceof LogicalOpJoin;
    }

    public static boolean matchTPAdd( final PhysicalOperator op ) {
        final PhysicalOperatorForLogicalOperator lop = (PhysicalOperatorForLogicalOperator) op;
        return lop.getLogicalOperator() instanceof LogicalOpTPAdd;
    }

    public static boolean matchBGPAdd( final PhysicalOperator op ) {
        final PhysicalOperatorForLogicalOperator lop = (PhysicalOperatorForLogicalOperator) op;
        return lop.getLogicalOperator() instanceof LogicalOpBGPAdd;
    }

    public static boolean matchUnion( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        return lop instanceof LogicalOpUnion;
    }

    public static boolean matchMultiwayJoin( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        return lop instanceof LogicalOpMultiwayJoin;
    }

    public static boolean matchMultiwayUnion( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        return lop instanceof LogicalOpMultiwayUnion;
    }

    public static boolean isBGPAddWithFm( final PhysicalOperator op, final FederationMember fm) {
        if ( matchBGPAdd(op) ) {
            final LogicalOpBGPAdd lop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

            return ( lop.getFederationMember() == fm );
        }
        return false;
    }

}
