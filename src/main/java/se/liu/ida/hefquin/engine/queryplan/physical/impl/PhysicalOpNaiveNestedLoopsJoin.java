package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpNaiveNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;

public class PhysicalOpNaiveNestedLoopsJoin extends BasePhysicalOpBinaryInputJoin {
    public PhysicalOpNaiveNestedLoopsJoin(LogicalOpJoin lop) {
        super(lop);
    }

    @Override
    public BinaryExecutableOp createExecOp(ExpectedVariables... inputVars) {
        return new ExecOpNaiveNestedLoopsJoin();
    }
}
