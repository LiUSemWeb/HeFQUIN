package se.liu.ida.hefquin.queryplan.logical;

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

}
