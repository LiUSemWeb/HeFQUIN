package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

public class LogicalOpMultiwayUnion implements NaryLogicalOp
{
	protected static LogicalOpMultiwayUnion singleton = new LogicalOpMultiwayUnion();

	public static LogicalOpMultiwayUnion getInstance() { return singleton; }

	protected LogicalOpMultiwayUnion() {}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
