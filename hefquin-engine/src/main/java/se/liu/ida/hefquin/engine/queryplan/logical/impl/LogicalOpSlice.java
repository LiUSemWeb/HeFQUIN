package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpSlice extends BaseForLogicalOps implements UnaryLogicalOp
{
	protected final int offset;
	protected final int limit;

	public LogicalOpSlice( final int limit,
	                       final boolean mayReduce ) {
		this(0, limit, mayReduce);
	}

	public LogicalOpSlice( final int offset,
	                       final int limit,
	                       final boolean mayReduce ) {
		super(mayReduce);

		assert offset >= 0;

		this.offset = offset;
		this.limit = limit;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		return inputVars[0];
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return    o instanceof LogicalOpSlice oo
		       && oo.offset == offset
		       && oo.limit == limit
		       && oo.mayReduce == mayReduce;
	}

	@Override
	public int hashCode(){
		return getClass().hashCode() ^ offset ^ limit;
	}

	@Override
	public String toString() {
		return "Slice (offset: " + offset + ", limit: " + limit + ")";
	}
}
