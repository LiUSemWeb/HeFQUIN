package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpBind extends LogicalOperatorBase implements UnaryLogicalOp
{
	protected final VarExprList bindExpressions;

	public LogicalOpBind( final VarExprList bindExpressions ) {
		assert bindExpressions != null;
		assert ! bindExpressions.isEmpty();

		this.bindExpressions = bindExpressions;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final ExpectedVariables expVarsInput = inputVars[0];

		final Set<Var> certainVars = expVarsInput.getCertainVariables();

		// The variable in a BIND clause is only possible, not certain,
		// because the evaluating the expression of the BIND clause may
		// result in an error, in which case the BIND variable remains
		// unbound.
		final Set<Var> possibleVars = new HashSet<>( expVarsInput.getPossibleVariables() );
		for ( final Var bindVar : bindExpressions.getVars() ) {
			if ( ! certainVars.contains(bindVar) ) {
				possibleVars.add(bindVar);
			}
		}

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;
		if ( ! (o instanceof LogicalOpBind) ) return false;

		final LogicalOpBind oo = (LogicalOpBind) o;
		return oo.bindExpressions.equals(bindExpressions);
	}

	@Override
	public int hashCode(){
		return bindExpressions.hashCode();
	}

	public VarExprList getBindExpressions() {
		return bindExpressions;
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "> Bind ( " + bindExpressions.toString() + " )";
	}

}