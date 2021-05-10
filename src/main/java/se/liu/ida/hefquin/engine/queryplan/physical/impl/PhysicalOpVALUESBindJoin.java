package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public class PhysicalOpVALUESBindJoin implements UnaryPhysicalOpForLogicalOp {
	
	protected final BinaryLogicalOp lop;
	
	protected PhysicalOpVALUESBindJoin( final BinaryLogicalOp lop) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public ExpectedVariables getExpectedVariables(ExpectedVariables... inputVars) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UnaryExecutableOp createExecOp(ExpectedVariables... inputVars) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UnaryLogicalOp getLogicalOperator() {
		// TODO Auto-generated method stub
		return null;
	}

}
