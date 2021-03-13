package se.liu.ida.hefquin.queryplan.logical;

import java.util.List;

import se.liu.ida.hefquin.queryplan.LogicalOperator;

public abstract class NaryLogicalOpImpl implements NaryLogicalOp
{
	private List<LogicalOperator> children;

	protected NaryLogicalOpImpl( final List<LogicalOperator> children ) {
		assert children != null;
		assert ! children.isEmpty();

		this.children = children;
	}

	public List<LogicalOperator> getChildren() {
		return children;
	}
}
