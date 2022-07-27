package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Objects;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

/**
 * A multiway left join corresponds to a sequence of SPARQL OPTIONAL clauses.
 * Hence, it is not to be confused with nested OPTIONAL clauses (which would,
 * instead, be captured as multiple nested multiway left joins).
 */
public class LogicalOpMultiwayLeftJoin implements NaryLogicalOp
{
	protected static LogicalOpMultiwayLeftJoin singleton = new LogicalOpMultiwayLeftJoin();

	public static LogicalOpMultiwayLeftJoin getInstance() { return singleton; }

	protected LogicalOpMultiwayLeftJoin() {}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpMultiwayLeftJoin; 
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
		return "> mlj ";
	}

}
