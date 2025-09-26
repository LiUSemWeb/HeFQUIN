package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpNaiveNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
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
	public PhysicalOpNaiveNestedLoopsJoin( final LogicalOpJoin lop ) {
		super(lop);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpNaiveNestedLoopsJoin && ((PhysicalOpNaiveNestedLoopsJoin) o).lop.equals(lop);
	}

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
	public String toString() {
		return "> naiveNestedLoop ";
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables inputVars ) {
			return ( lop instanceof LogicalOpJoin );
		}

		@Override
		public PhysicalOperator create( final LogicalOperator lop ) {
			if ( lop instanceof LogicalOpJoin op ) {
				return new PhysicalOpNaiveNestedLoopsJoin(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
