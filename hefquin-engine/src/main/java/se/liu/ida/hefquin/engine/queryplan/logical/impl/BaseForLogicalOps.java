package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;

public abstract class BaseForLogicalOps implements LogicalOperator
{
	final static boolean DISABLE_MAY_REDUCE = Boolean
		.parseBoolean( System.getenv().getOrDefault("DISABLE_MAY_REDUCE", "false") );

	protected final boolean mayReduce;

	public BaseForLogicalOps( final boolean mayReduce ) {
		if ( DISABLE_MAY_REDUCE )
			this.mayReduce = false;
		else
			this.mayReduce = mayReduce;
	}

	@Override
	public boolean mayReduce() {
		return mayReduce;
	}
}
