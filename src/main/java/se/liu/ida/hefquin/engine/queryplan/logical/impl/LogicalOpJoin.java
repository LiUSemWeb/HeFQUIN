package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

import java.util.Objects;

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
	public int hashCode(){
		return Objects.hash( this.getClass().getName() );
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
