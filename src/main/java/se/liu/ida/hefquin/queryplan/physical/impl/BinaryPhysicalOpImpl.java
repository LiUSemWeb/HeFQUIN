package se.liu.ida.hefquin.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.queryplan.physical.BinaryPhysicalOp;

public abstract class BinaryPhysicalOpImpl implements BinaryPhysicalOp
{
	private final PhysicalOperator childOp1;
	private final PhysicalOperator childOp2;

	protected BinaryPhysicalOpImpl(
			final PhysicalOperator childOp1,
			final PhysicalOperator childOp2 )
	{
		assert childOp1 != null;
		assert childOp2 != null;

		this.childOp1 = childOp1;
		this.childOp2 = childOp2;
	}

	@Override
	public PhysicalOperator getChildOp1() {
		return childOp1;
	}

	@Override
	public PhysicalOperator getChildOp2() {
		return childOp2;
	}

	@Override
	public int numberOfChildren() { return 2; }

	@Override
	public PhysicalOperator getChild( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return childOp1;
		else if ( i == 1 )
			return childOp2;
		else
			throw new NoSuchElementException( "this operator does not have a " + i + "-th child" );
	}

}
