package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpDedup extends BaseForLogicalOps implements UnaryLogicalOp 
{
	protected static final LogicalOpDedup singletonFalse = new LogicalOpDedup(false);
	protected static final LogicalOpDedup singletonTrue  = new LogicalOpDedup(true);

	public static LogicalOpDedup getInstance( final boolean mayReduce ) {
		return mayReduce ? singletonTrue : singletonFalse;
	}

	protected LogicalOpDedup( final boolean mayReduce ) {
		super( mayReduce );
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		return inputVars[0];
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpDedup; 
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "dedup";
	}
}
