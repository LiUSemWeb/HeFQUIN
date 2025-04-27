package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOpForLogicalOp;

/**
 * Base class for physical operators that implement
 * some form of a multi-way join algorithm.
 */
public abstract class BaseForPhysicalOpMultiwayJoin extends BaseForPhysicalOps implements NaryPhysicalOpForLogicalOp
{
	protected final LogicalOpMultiwayJoin lop;

	protected BaseForPhysicalOpMultiwayJoin( final LogicalOpMultiwayJoin lop ) {
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
