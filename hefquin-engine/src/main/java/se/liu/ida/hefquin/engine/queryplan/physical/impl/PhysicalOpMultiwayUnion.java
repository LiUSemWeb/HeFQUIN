package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements a multi-way union.
 *
 * The actual algorithm of this operator is implemented
 * in the {@link ExecOpMultiwayUnion} class.
 */
public class PhysicalOpMultiwayUnion extends BaseForQueryPlanOperator
                                     implements NaryPhysicalOpForLogicalOp
{
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
