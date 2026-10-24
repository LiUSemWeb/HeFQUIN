package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;

public abstract class BaseForLogicalOps implements LogicalOperator
{
	protected final boolean mayReduce;

	public BaseForLogicalOps( final boolean mayReduce ) {
		this.mayReduce = mayReduce;
	}

	@Override
	public boolean mayReduce() {
		return mayReduce;
	}
}
