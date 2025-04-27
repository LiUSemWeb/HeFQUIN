package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements a multi-way union.
 *
 * The actual algorithm of this operator is implemented
 * in the {@link ExecOpMultiwayUnion} class.
 */
public class PhysicalOpMultiwayUnion extends BaseForPhysicalOps
                                     implements NaryPhysicalOpForLogicalOp
{
	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		return getLogicalOperator().getExpectedVariables(inputVars);
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public NaryExecutableOp createExecOp( final boolean collectExceptions,
	                                      final ExpectedVariables... inputVars) {
		return new ExecOpMultiwayUnion( inputVars.length, collectExceptions );
	}

	@Override
	public NaryLogicalOp getLogicalOperator() {
		return LogicalOpMultiwayUnion.getInstance();
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpMultiwayUnion;
	}

	@Override
	public int hashCode() {
		return LogicalOpMultiwayUnion.getInstance().hashCode();
	}

	@Override
	public String toString(){
		return "> multiwayUnion " + "(" + getID() + ")";
	}

}
