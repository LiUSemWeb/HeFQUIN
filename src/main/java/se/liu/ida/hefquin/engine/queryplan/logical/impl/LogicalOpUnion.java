package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpUnion implements BinaryLogicalOp
{
	protected static LogicalOpUnion singleton = new LogicalOpUnion();

	public static LogicalOpUnion getInstance() { return singleton; }

	protected LogicalOpUnion() {}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpUnion; 
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		return "> union ";
	}

}
