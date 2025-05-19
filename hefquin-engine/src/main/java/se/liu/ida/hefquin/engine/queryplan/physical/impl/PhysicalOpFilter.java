package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

/**
 * A physical operator that filters the input solution mappings.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpFilter} class.
 */
public class PhysicalOpFilter extends BaseForPhysicalOps implements UnaryPhysicalOpForLogicalOp
{
	protected final LogicalOpFilter lop;

	public PhysicalOpFilter( final LogicalOpFilter lop ) {
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
		return new ExecOpFilter( lop.getFilterExpressions(), collectExceptions );
	}

	@Override
	public UnaryLogicalOp getLogicalOperator() {
		return lop;
	}

}
