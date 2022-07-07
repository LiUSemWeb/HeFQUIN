package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpHashJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

public class PhysicalOpHashJoin extends BasePhysicalOpBinaryJoin
{
    public PhysicalOpHashJoin( final LogicalOpJoin lop ) {
        super(lop);
    }

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpHashJoin && ((PhysicalOpHashJoin) o).lop.equals(lop);
	}

    @Override
    public BinaryExecutableOp createExecOp( final ExpectedVariables ... inputVars ) {
        assert inputVars.length == 2;

        return new ExecOpHashJoin( inputVars[0], inputVars[1] );
    }

    @Override
    public void visit(final PhysicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString(){
        return "> hashJoin ";
    }

}