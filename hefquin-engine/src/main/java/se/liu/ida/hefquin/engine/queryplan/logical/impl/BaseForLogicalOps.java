package se.liu.ida.hefquin.engine.queryplan.logical.impl;

public abstract class BaseForLogicalOps 
{
	protected final boolean mayReduce;

	public BaseForLogicalOps( boolean mayReduce ) {
		this.mayReduce = mayReduce;
	}

	public boolean mayReduce() {
		return mayReduce;
	}
}
