package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Objects;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpHashRJoin;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements the hash join algorithm to perform
 * a right outer join of two sequences of input solution mappings (produced
 * by the two sub-plans under this operator). The hash join algorithm builds
 * a hash table with the solution mappings of the first input sequence (using
 * the values that they have for the join variables to decide where to place
 * them in the hash table) and, thereafter, probes the hash table to find join
 * partners for each of the solution mappings of the second input sequence.
 * As this operator performs a right outer join, the solution mappings of the
 * second input sequence that do not have a join partner are not discarded (as
 * would be the case for an inner join) but, instead, are passed to the output
 * as is.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpHashRJoin} class.
 */
public class PhysicalOpHashRJoin extends BaseForQueryPlanOperator
                                 implements BinaryPhysicalOpForLogicalOp
{
	protected final LogicalOpRightJoin lop;

	public PhysicalOpHashRJoin(final LogicalOpRightJoin lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public LogicalOpRightJoin getLogicalOperator() {
		return lop;
	}

	@Override
	public BinaryExecutableOp createExecOp( final boolean collectExceptions,
	                                        final QueryPlanningInfo qpInfo,
	                                        final ExpectedVariables ... inputVars ) {
		assert inputVars.length == 2;

		return new ExecOpHashRJoin( inputVars[0], inputVars[1], collectExceptions, qpInfo );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpHashRJoin && ((PhysicalOpHashRJoin) o).lop.equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode() ^ Objects.hash( this.getClass().getName() );
	}

	@Override
	public String toString(){
		return "> hashRJoin ";
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpRightJoin );
		}

		@Override
		public PhysicalOperator create( final LogicalOperator lop ) {
			if ( lop instanceof LogicalOpRightJoin op ) {
				return new PhysicalOpHashRJoin(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
