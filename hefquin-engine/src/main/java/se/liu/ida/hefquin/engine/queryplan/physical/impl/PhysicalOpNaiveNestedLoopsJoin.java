package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpNaiveNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements a local (!) nested loops
 * join algorithm to perform an inner join of two sequences of
 * input solution mappings (produced by the two sub-plans under
 * this operator). Hence, this is the most naive type of binary
 * join algorithm, nothing fancy.
 *
 * For a slightly more detailed description of the actual
 * algorithm associated with this physical operator, refer
 * to {@link ExecOpNaiveNestedLoopsJoin}, which provides the
 * implementation of this algorithm.
 */
public class PhysicalOpNaiveNestedLoopsJoin extends BaseForPhysicalOpBinaryJoin
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	private static PhysicalOpNaiveNestedLoopsJoin singleton = null;

	protected PhysicalOpNaiveNestedLoopsJoin() { }

	@Override
	public BinaryExecutableOp createExecOp( final boolean collectExceptions,
	                                        final QueryPlanningInfo qpInfo,
	                                        final ExpectedVariables... inputVars ) {
		return new ExecOpNaiveNestedLoopsJoin(collectExceptions, qpInfo);
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof PhysicalOpNaiveNestedLoopsJoin;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ getLogicalOperator().hashCode();
	}

	@Override
	public String toString() {
		return "naiveNLJ";
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpJoin );
		}

		@Override
		public PhysicalOpNaiveNestedLoopsJoin create( final BinaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpJoin ) return getInstance();

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

	public static PhysicalOpNaiveNestedLoopsJoin getInstance() {
		if ( singleton == null ) singleton = new PhysicalOpNaiveNestedLoopsJoin();

		return singleton;
	}
}
