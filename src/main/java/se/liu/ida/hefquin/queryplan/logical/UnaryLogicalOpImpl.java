package se.liu.ida.hefquin.queryplan.logical;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.LogicalOperator;

public abstract class UnaryLogicalOpImpl implements UnaryLogicalOp
{
	private final LogicalOperator childOp;

	protected UnaryLogicalOpImpl( final LogicalOperator childOp ) {
		assert childOp != null;
		this.childOp = childOp;
	}

	public LogicalOperator getChildOp() {
		return childOp;
	}

	@Override
	public int numberOfChildren() { return 1; }

	@Override
	public LogicalOperator getChild( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return childOp;
		else
			throw new NoSuchElementException( "this operator does not have a " + i + "-th child" );
	}

}
