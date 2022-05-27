package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public class PhysicalOpLocalToGlobal implements UnaryPhysicalOpForLogicalOp {

	protected final LogicalOpLocalToGlobal logicall2g;
	
	public PhysicalOpLocalToGlobal (final LogicalOpLocalToGlobal l2g) {
		this.logicall2g = l2g;
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
		return new ExecOpLocalToGlobal( logicall2g.getVocabularyMapping() );
	}

	@Override
	public UnaryLogicalOp getLogicalOperator() {
		return this.logicall2g;
	}

	@Override
	public String toString() {
		return "> l2g " + "(vocab.mapping: " + logicall2g.getVocabularyMapping().hashCode() + ")";
	}

}
