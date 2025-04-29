package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public class PhysicalOpBind extends BaseForPhysicalOps implements UnaryPhysicalOpForLogicalOp
{
	protected final LogicalOpBind lop;

	public PhysicalOpBind( final LogicalOpBind lop ) {
		this.lop = lop;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		return lop.getExpectedVariables(inputVars);
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final ExpectedVariables... inputVars ) {
		return new ExecOpBind( lop.getBindExpressions(), collectExceptions );
	}

	@Override
	public UnaryLogicalOp getLogicalOperator() {
		return lop;
	}

}
