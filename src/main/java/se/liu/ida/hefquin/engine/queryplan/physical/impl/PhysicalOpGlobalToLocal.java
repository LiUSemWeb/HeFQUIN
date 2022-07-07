package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public class PhysicalOpGlobalToLocal implements UnaryPhysicalOpForLogicalOp {

	protected final LogicalOpGlobalToLocal logicalg2l;
	
	public PhysicalOpGlobalToLocal (final LogicalOpGlobalToLocal g2l) {
		this.logicalg2l = g2l;
	}
	
	@Override
	public ExpectedVariables getExpectedVariables(final ExpectedVariables... inputVars) {
		if(inputVars.length == 1) {
			return inputVars[0];
		} else {
			throw new IllegalArgumentException("There is more than 1 input variable.");
		}
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
		
	}

	@Override
	public UnaryExecutableOp createExecOp(final ExpectedVariables... inputVars) {
		return new ExecOpGlobalToLocal( logicalg2l.getVocabularyMapping() );
	}

	@Override
	public UnaryLogicalOp getLogicalOperator() {
		return this.logicalg2l;
	}

	@Override
	public String toString() {
		return "> g2l " + "(vocab.mapping: " + logicalg2l.getVocabularyMapping().hashCode() + ")";
	}

}
