package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements a multi-way union.
 *
 * The actual algorithm of this operator is implemented
 * in the {@link ExecOpMultiwayUnion} class.
 */
public class PhysicalOpMultiwayUnion implements NaryPhysicalOpForLogicalOp
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public NaryExecutableOp createExecOp( final boolean collectExceptions,
	                                      final QueryPlanningInfo qpInfo,
	                                      final ExpectedVariables... inputVars) {
		return new ExecOpMultiwayUnion( inputVars.length, collectExceptions, qpInfo );
	}

	@Override
	public LogicalOpMultiwayUnion getLogicalOperator() {
		return LogicalOpMultiwayUnion.getInstance();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof PhysicalOpMultiwayUnion;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ getLogicalOperator().hashCode();
	}

	@Override
	public String toString() {
		return "mu";
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpMultiwayUnion );
		}

		@Override
		public PhysicalOpMultiwayUnion create( final NaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpMultiwayUnion ) {
				return new PhysicalOpMultiwayUnion();
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
