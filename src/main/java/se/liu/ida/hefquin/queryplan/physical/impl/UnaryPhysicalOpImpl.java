package se.liu.ida.hefquin.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.queryplan.physical.UnaryPhysicalOp;

public abstract class UnaryPhysicalOpImpl implements UnaryPhysicalOp 
{
	private final PhysicalOperator childOp;

	protected UnaryPhysicalOpImpl( final PhysicalOperator childOp )
	{
		assert childOp != null;
		this.childOp = childOp;
	}

	@Override
	public PhysicalOperator getChildOp() {
		return childOp;
	}

	@Override
	public int numberOfChildren() { return 1; }

	@Override
	public PhysicalOperator getChild( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return childOp;
		else
			throw new NoSuchElementException( "this operator does not have a " + i + "-th child" );
	}

}
