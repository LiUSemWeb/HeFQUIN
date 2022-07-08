package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

import java.util.Objects;

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
	public int hashCode(){
		return Objects.hash( this.getClass().getName() );
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		return "> mj ";
	}

}
