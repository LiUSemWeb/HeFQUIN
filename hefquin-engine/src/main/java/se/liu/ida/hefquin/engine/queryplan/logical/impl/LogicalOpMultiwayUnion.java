package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class LogicalOpMultiwayUnion extends LogicalOperatorBase implements NaryLogicalOp
{
	protected static LogicalOpMultiwayUnion singleton = new LogicalOpMultiwayUnion();

	public static LogicalOpMultiwayUnion getInstance() { return singleton; }

	protected LogicalOpMultiwayUnion() {}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		final Set<Var> certainVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars);
		final Set<Var> possibleVars = ExpectedVariablesUtils.unionOfAllVariables(inputVars);
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

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
		return "mu (" + getID() + ") ";
	}

}
