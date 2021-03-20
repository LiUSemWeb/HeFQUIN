package se.liu.ida.hefquin.queryplan.logical.impl;

import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.LogicalOperator;
import se.liu.ida.hefquin.queryplan.logical.NaryLogicalOp;

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

	@Override
	public int numberOfChildren() { return children.size(); }

	@Override
	public LogicalOperator getChild( final int i ) throws NoSuchElementException {
		if ( i >= children.size() )
			throw new NoSuchElementException( "this operator does not have a " + i + "-th child" );
		else
			return children.get(i);
	}
}
