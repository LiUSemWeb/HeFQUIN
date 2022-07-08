package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

import java.util.Objects;

public class LogicalOpMultiwayUnion implements NaryLogicalOp
{
	protected static LogicalOpMultiwayUnion singleton = new LogicalOpMultiwayUnion();

	public static LogicalOpMultiwayUnion getInstance() { return singleton; }

	protected LogicalOpMultiwayUnion() {}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpMultiwayUnion;
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
		return "> mu ";
	}

}
