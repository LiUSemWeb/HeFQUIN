package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class LogicalOpUnion extends BaseForLogicalOps implements BinaryLogicalOp
{
	protected static final LogicalOpUnion singletonFalse = new LogicalOpUnion(false);
	protected static final LogicalOpUnion singletonTrue  = new LogicalOpUnion(true);

	public static LogicalOpUnion getInstance( final boolean mayReduce ) {
		return mayReduce ? singletonTrue : singletonFalse;
	}
	
	public static LogicalOpUnion getInstance() {
		return singletonFalse;
	}

	protected LogicalOpUnion( final boolean mayReduce ) {
		super( mayReduce );
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

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
		return o instanceof LogicalOpUnion; 
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "union";
	}
}
