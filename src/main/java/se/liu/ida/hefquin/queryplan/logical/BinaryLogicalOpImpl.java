package se.liu.ida.hefquin.queryplan.logical;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.LogicalOperator;

public abstract class BinaryLogicalOpImpl implements BinaryLogicalOp
{
	private final LogicalOperator childOp1;
	private final LogicalOperator childOp2;

	protected BinaryLogicalOpImpl( final LogicalOperator childOp1, final LogicalOperator childOp2 ) {
		assert childOp1 != null;
		assert childOp2 != null;

		this.childOp1 = childOp1;
		this.childOp2 = childOp2;
	}

	public LogicalOperator getChildOp1() {
		return childOp1;
	}

	public LogicalOperator getChildOp2() {
		return childOp2;
	}

	@Override
	public int numberOfChildren() { return 2; }

	@Override
	public LogicalOperator getChild( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return childOp1;
		else if ( i == 1 )
			return childOp2;
		else
			throw new NoSuchElementException( "this operator does not have a " + i + "-th child" );
	}

}
