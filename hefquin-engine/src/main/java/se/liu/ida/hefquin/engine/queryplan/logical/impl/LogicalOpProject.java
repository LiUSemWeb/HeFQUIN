package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpProject extends BaseForLogicalOps implements UnaryLogicalOp
{
	protected final Set<Var> variables;

	public LogicalOpProject( final List<Var> variables, final boolean mayReduce ) {
		this( new HashSet<>(variables), mayReduce );
	}

	public LogicalOpProject( final Set<Var> variables, final boolean mayReduce ) {
		super( mayReduce );

		assert variables != null;
		assert ! variables.isEmpty();

		this.variables = variables;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final Set<Var> certainVars = new HashSet<>( inputVars[0].getCertainVariables() );
		certainVars.retainAll(variables);
		final Set<Var> possibleVars = new HashSet<>( inputVars[0].getPossibleVariables() );
		possibleVars.retainAll(variables);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	public Set<Var> getVariables() {
		return variables;
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return    o instanceof LogicalOpProject oo
		       && oo.variables.equals(variables);
	}

	@Override
	public int hashCode(){
		return getClass().hashCode() ^ variables.hashCode();
	}

	@Override
	public String toString() {
		return "project ( " + variables.toString() + " )";
	}

}
