package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Objects;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpRightJoin implements BinaryLogicalOp
{
	protected static LogicalOpRightJoin singleton = new LogicalOpRightJoin();

	public static LogicalOpRightJoin getInstance() { return singleton; }

	protected LogicalOpRightJoin() {}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpRightJoin; 
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
