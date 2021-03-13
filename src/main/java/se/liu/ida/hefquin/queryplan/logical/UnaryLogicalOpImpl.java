package se.liu.ida.hefquin.queryplan.logical;

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
}
