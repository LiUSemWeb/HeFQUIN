package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpHashJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;

public class PhysicalOpHashJoin extends BasePhysicalOpBinaryJoin {
    public PhysicalOpHashJoin(final LogicalOpJoin lop) {
        super(lop);
    }

    @Override
    public BinaryExecutableOp createExecOp( final ExpectedVariables ... inputVars ) {
        assert inputVars.length == 2;

        return new ExecOpHashJoin( inputVars[0], inputVars[1] );
    }
}