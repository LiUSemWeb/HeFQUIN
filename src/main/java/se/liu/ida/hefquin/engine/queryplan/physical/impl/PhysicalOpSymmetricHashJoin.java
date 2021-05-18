package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSymmetricHashJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;

public class PhysicalOpSymmetricHashJoin extends BasePhysicalOpBinaryJoin {
    public PhysicalOpSymmetricHashJoin(final LogicalOpJoin lop) {
        super(lop);
    }

    @Override
    public BinaryExecutableOp createExecOp( final ExpectedVariables ... inputVars ) {
        assert inputVars.length == 2;

        return new ExecOpSymmetricHashJoin( inputVars[0], inputVars[1] );
    }
}