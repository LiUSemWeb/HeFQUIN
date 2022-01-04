package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpJoin implements BinaryLogicalOp
{
	protected static LogicalOpJoin singleton = new LogicalOpJoin();

	public static LogicalOpJoin getInstance() { return singleton; }

	protected LogicalOpJoin() {}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpJoin; 
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		return "> join ";
	}

}
