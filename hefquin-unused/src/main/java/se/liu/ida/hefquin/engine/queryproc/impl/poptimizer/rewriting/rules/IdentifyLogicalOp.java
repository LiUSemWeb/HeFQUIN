package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.federation.FederationMember;

public class IdentifyLogicalOp {

    public static boolean isJoin( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        return lop instanceof LogicalOpJoin;
    }

    public static boolean isTPAdd( final PhysicalOperator op ) {
        final PhysicalOperatorForLogicalOperator lop = (PhysicalOperatorForLogicalOperator) op;
        return lop.getLogicalOperator() instanceof LogicalOpTPAdd;
    }

    public static boolean isBGPAdd( final PhysicalOperator op ) {
        final PhysicalOperatorForLogicalOperator lop = (PhysicalOperatorForLogicalOperator) op;
        return lop.getLogicalOperator() instanceof LogicalOpBGPAdd;
    }

    public static boolean isUnion( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        return lop instanceof LogicalOpUnion;
    }

    public static boolean isMultiwayJoin( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        return lop instanceof LogicalOpMultiwayJoin;
    }

    public static boolean isMultiwayUnion( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        return lop instanceof LogicalOpMultiwayUnion;
    }

    public static boolean isBGPAddWithFm( final PhysicalOperator op, final FederationMember fm) {
        if ( isBGPAdd(op) ) {
            final LogicalOpBGPAdd lop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

            return ( lop.getFederationMember() == fm );
        }
        return false;
    }

}
