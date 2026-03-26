package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpDuplicateRemoval;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpDedup;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public class PhysicalOpDuplicateRemoval implements UnaryPhysicalOpForLogicalOp
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	private static PhysicalOpDuplicateRemoval singleton = null;

	protected PhysicalOpDuplicateRemoval() { }

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables... inputVars ) {
		return new ExecOpDuplicateRemoval(collectExceptions, qpInfo);
	}

	@Override
	public LogicalOpDedup getLogicalOperator() {
		return LogicalOpDedup.getInstance();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof PhysicalOpDuplicateRemoval;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ LogicalOpDedup.getInstance().hashCode();
	}

	@Override
	public String toString() {
		return "dedup";
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpDedup );
		}

		@Override
		public PhysicalOpDuplicateRemoval create( final UnaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpDedup ) return getInstance();

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

	public static PhysicalOpDuplicateRemoval getInstance() {
		if ( singleton == null ) singleton = new PhysicalOpDuplicateRemoval();

		return singleton;
	}
}
