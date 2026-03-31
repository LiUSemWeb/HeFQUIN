package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class LogicalOpMultiwayUnion extends BaseForLogicalOps implements NaryLogicalOp
{
	protected static final LogicalOpMultiwayUnion singletonFalse = new LogicalOpMultiwayUnion(false);
	protected static final LogicalOpMultiwayUnion singletonTrue  = new LogicalOpMultiwayUnion(true);

	public static LogicalOpMultiwayUnion getInstance( final boolean mayReduce ) {
		return mayReduce ? singletonTrue : singletonFalse;
	}

	public static LogicalOpMultiwayUnion getInstance() {
		return singletonFalse;
	}

	protected LogicalOpMultiwayUnion( final boolean mayReduce ) {
		super( mayReduce );
	}

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
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpMultiwayUnion;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "mu";
	}
}
