package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpUnion implements BinaryLogicalOp
{
	protected static LogicalOpUnion singleton = new LogicalOpUnion();

	public static LogicalOpUnion getInstance() { return singleton; }

	protected LogicalOpUnion() {}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
