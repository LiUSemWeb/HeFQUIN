package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements a binary union.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpBinaryUnion} class.
 */
public class PhysicalOpBinaryUnion extends BaseForQueryPlanOperator
                                   implements BinaryPhysicalOpForLogicalOp
{
	protected final LogicalOpUnion lop;

	public PhysicalOpBinaryUnion( final LogicalOpUnion lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBinaryUnion && ((PhysicalOpBinaryUnion) o).lop.equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode();
	}

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
		return lop;
	}

	@Override
	public String toString(){
		return "> binaryUnion ";
	}

}
