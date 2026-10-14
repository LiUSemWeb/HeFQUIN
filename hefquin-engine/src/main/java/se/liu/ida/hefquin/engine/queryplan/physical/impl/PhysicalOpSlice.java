package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpSlice;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

/**
 * A physical operator that applies an offset and a limit to the
 * sequence of input solution mappings.
 */
public class PhysicalOpSlice implements UnaryPhysicalOpForLogicalOp
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	protected final LogicalOpSlice lop;

	protected PhysicalOpSlice( final LogicalOpSlice lop ) {
		this.lop = lop;
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables ... inputVars ) {
		throw new UnsupportedOperationException("OFFSET & LIMIT needs to be captured directly in the executable plan.");
	}

	@Override
	public LogicalOpSlice getLogicalOperator() {
		return lop;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return    o instanceof PhysicalOpSlice oo
		       && oo.lop.equals(lop);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ lop.hashCode();
	}

	@Override
	public String toString() {
		return lop.toString();
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpSlice );
		}

		@Override
		public PhysicalOpSlice create( final UnaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpSlice op ) {
				return new PhysicalOpSlice(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

}
