package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements a binary union.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpBinaryUnion} class.
 */
public class PhysicalOpBinaryUnion
		implements BinaryPhysicalOpForLogicalOp
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	private static PhysicalOpBinaryUnion singleton = null;

	protected PhysicalOpBinaryUnion() { }

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public BinaryExecutableOp createExecOp( final boolean collectExceptions,
	                                        final QueryPlanningInfo qpInfo,
	                                        final ExpectedVariables... inputVars ) {
		return new ExecOpBinaryUnion(collectExceptions, qpInfo);
	}

	@Override
	public LogicalOpUnion getLogicalOperator() {
		return LogicalOpUnion.getInstance();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof PhysicalOpBinaryUnion;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ LogicalOpUnion.getInstance().hashCode();
	}

	@Override
	public String toString() {
		return "union";
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpUnion );
		}

		@Override
		public PhysicalOpBinaryUnion create( final BinaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpUnion ) return getInstance();

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

	public static PhysicalOpBinaryUnion getInstance() {
		if ( singleton == null ) singleton = new PhysicalOpBinaryUnion();

		return singleton;
	}
}
