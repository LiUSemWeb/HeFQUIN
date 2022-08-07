package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public class PhysicalOpFilter  implements UnaryPhysicalOpForLogicalOp {

	protected final LogicalOpFilter logicalFilter;
	
	public PhysicalOpFilter( final LogicalOpFilter lf ) {
		this.logicalFilter = lf;
	}
	
	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		if(inputVars.length == 1) {
			return inputVars[0];
		} else {
			throw new IllegalArgumentException("There is more than 1 input variable.");
		}
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final ExpectedVariables... inputVars ) {
		return new ExecOpFilter( logicalFilter.getFilterExpressions(), collectExceptions );
	}

	@Override
	public UnaryLogicalOp getLogicalOperator() {
		return logicalFilter;
	}

}
