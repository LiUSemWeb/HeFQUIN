package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpHashJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements the hash join algorithm to perform an
 * inner join of two sequences of input solution mappings (produced by the two
 * sub-plans under this operator). The hash join algorithm builds a hash table
 * with the solution mappings of the first input sequence (using the values
 * that they have for the join variables to decide where to place them in the
 * hash table) and, thereafter, probes the hash table to find join partners for
 * each of the solution mappings of the second input sequence.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpHashJoin} class.
 */
public class PhysicalOpHashJoin extends BaseForPhysicalOpBinaryJoin
{
	public PhysicalOpHashJoin( final LogicalOpJoin lop ) {
		super(lop);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpHashJoin && ((PhysicalOpHashJoin) o).lop.equals(lop);
	}

	@Override
	public BinaryExecutableOp createExecOp( final boolean collectExceptions,
	                                        final ExpectedVariables ... inputVars ) {
		assert inputVars.length == 2;

		return new ExecOpHashJoin( inputVars[0], inputVars[1], collectExceptions );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "> hashJoin ";
	}
}
