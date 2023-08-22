package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOpForLogicalOp;

public abstract class BasePhysicalOpMultiwayLeftJoin implements NaryPhysicalOpForLogicalOp
{
	protected final LogicalOpMultiwayLeftJoin lop;

	protected BasePhysicalOpMultiwayLeftJoin( final LogicalOpMultiwayLeftJoin lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof NaryPhysicalOpForLogicalOp
				&& ((NaryPhysicalOpForLogicalOp) o).getLogicalOperator().equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode();
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		return lop.getExpectedVariables(inputVars);
	}

	@Override
	public NaryLogicalOp getLogicalOperator() {
		return lop;
	}

}
