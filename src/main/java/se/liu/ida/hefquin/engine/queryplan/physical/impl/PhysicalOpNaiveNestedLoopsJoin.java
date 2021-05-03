package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpNaiveNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;

public class PhysicalOpNaiveNestedLoopsJoin extends BasePhysicalOpBinaryInputJoin {
    public PhysicalOpNaiveNestedLoopsJoin(BinaryLogicalOp lop) {
        super(lop);
    }

    @Override
    public BinaryExecutableOp createExecOp(ExpectedVariables... inputVars) {
        if ( lop instanceof LogicalOpJoin) {
            return new ExecOpNaiveNestedLoopsJoin();
        }
        else
            throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
    }
}
