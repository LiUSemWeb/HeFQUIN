package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpLeftJoin implements BinaryLogicalOp
{
	protected static LogicalOpLeftJoin singleton = new LogicalOpLeftJoin();

	public static LogicalOpLeftJoin getInstance() { return singleton; }

	protected LogicalOpLeftJoin() {}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		final Set<Var> certainVars = inputVars[0].getCertainVariables();

		final Set<Var> possibleVars = new HashSet<>();
		possibleVars.addAll( inputVars[1].getCertainVariables() );
		possibleVars.addAll( inputVars[1].getPossibleVariables() );
		possibleVars.addAll( inputVars[0].getPossibleVariables() );
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpLeftJoin; 
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "leftjoin";
	}
}
