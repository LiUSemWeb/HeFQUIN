package se.liu.ida.hefquin.queryplan.logical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.LogicalOperator;
import se.liu.ida.hefquin.queryplan.logical.NullaryLogicalOp;

public abstract class NullaryLogicalOpImpl implements NullaryLogicalOp
{
	@Override
	public int numberOfChildren() { return 0; }

	@Override
	public LogicalOperator getChild( final int i ) throws NoSuchElementException {
		throw new NoSuchElementException( "this operator does not have a " + i + "-th child" );
	}

}
