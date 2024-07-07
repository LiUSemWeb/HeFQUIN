package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;

import java.util.Objects;

/**
 * Base class for physical operators that implement
 * some form of a binary join algorithm.
 */
public abstract class BaseForPhysicalOpBinaryJoin extends BaseForPhysicalOps implements BinaryPhysicalOpForLogicalOp
{
	protected final LogicalOpJoin lop;

	protected BaseForPhysicalOpBinaryJoin( final LogicalOpJoin lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof BinaryPhysicalOpForLogicalOp
				&& ((BinaryPhysicalOpForLogicalOp) o).getLogicalOperator().equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode() ^ Objects.hash( this.getClass().getName() );
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		return lop.getExpectedVariables(inputVars);
	}

	@Override
	public BinaryLogicalOp getLogicalOperator() {
		return lop;
	}

}
