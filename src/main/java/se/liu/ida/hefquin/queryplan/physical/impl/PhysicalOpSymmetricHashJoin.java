package se.liu.ida.hefquin.queryplan.physical.impl;

import se.liu.ida.hefquin.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.ExecOpSymmetricHashJoin;
import se.liu.ida.hefquin.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.queryplan.physical.BinaryPhysicalOpForLogicalOp;

public class PhysicalOpSymmetricHashJoin implements BinaryPhysicalOpForLogicalOp {
    protected final BinaryLogicalOp lop;

    public PhysicalOpSymmetricHashJoin(final BinaryLogicalOp lop) {
        assert lop != null;
        assert (lop instanceof LogicalOpJoin);
        this.lop = lop;
    }

    @Override
    public BinaryExecutableOp createExecOp() {
        return new ExecOpSymmetricHashJoin();
    }

    @Override
    public BinaryLogicalOp getLogicalOperator() {
        return lop;
    }
}
