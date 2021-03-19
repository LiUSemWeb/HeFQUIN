package se.liu.ida.hefquin.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.queryplan.physical.NullaryPhysicalOp;

public abstract class NullaryPhysicalOpImpl implements NullaryPhysicalOp
{
	@Override
	public int numberOfChildren() { return 0; }

	@Override
	public PhysicalOperator getChild( final int i ) throws NoSuchElementException {
		throw new NoSuchElementException( "this operator does not have a " + i + "-th child" );
	}

}
