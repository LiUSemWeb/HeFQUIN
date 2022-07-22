package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Objects;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpLeftJoin implements BinaryLogicalOp
{
	protected static LogicalOpJoin singleton = new LogicalOpJoin();

	public static LogicalOpJoin getInstance() { return singleton; }

	protected LogicalOpLeftJoin() {}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpLeftJoin; 
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
		return "> leftjoin ";
	}

}
