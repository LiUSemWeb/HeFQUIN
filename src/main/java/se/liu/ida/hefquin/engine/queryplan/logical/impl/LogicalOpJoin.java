package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpJoin implements BinaryLogicalOp
{
	protected static LogicalOpJoin singleton = new LogicalOpJoin();

	public static LogicalOpJoin getInstance() { return singleton; }

	protected LogicalOpJoin() {}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
