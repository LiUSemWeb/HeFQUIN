package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

public class LogicalOpMultiwayJoin implements NaryLogicalOp
{
	protected static LogicalOpMultiwayJoin singleton = new LogicalOpMultiwayJoin();

	public static LogicalOpMultiwayJoin getInstance() { return singleton; }

	protected LogicalOpMultiwayJoin() {}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpMultiwayJoin; 
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
