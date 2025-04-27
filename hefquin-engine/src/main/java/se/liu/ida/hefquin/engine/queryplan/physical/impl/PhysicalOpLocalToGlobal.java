package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

/**
 * A physical operator that applies a given vocabulary mapping to all input
 * solution mappings, converting them from using local vocabulary terms to
 * using global vocabulary terms.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpLocalToGlobal} class.
 */
public class PhysicalOpLocalToGlobal extends BaseForPhysicalOps
                                     implements UnaryPhysicalOpForLogicalOp
{
	protected final LogicalOpLocalToGlobal lop;

	public PhysicalOpLocalToGlobal( final LogicalOpLocalToGlobal lop ) {
		this.lop = lop;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		return lop.getExpectedVariables(inputVars);
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final ExpectedVariables... inputVars ) {
		return new ExecOpLocalToGlobal( lop.getVocabularyMapping(), collectExceptions );
	}

	@Override
	public UnaryLogicalOp getLogicalOperator() {
		return this.lop;
	}

	@Override
	public String toString() {
		return "> l2g " + "(vocab.mapping: " + lop.getVocabularyMapping().hashCode() + ")";
	}

}
