package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpExtend implements UnaryLogicalOp
{
	protected final VarExprList extendExpressions;

	public LogicalOpExtend( final VarExprList extendExpressions ) {
		assert extendExpressions != null;
		assert ! extendExpressions.isEmpty();

		this.extendExpressions = extendExpressions;
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
		for ( final Var bindVar : extendExpressions.getVars() ) {
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
		if ( ! (o instanceof LogicalOpExtend) ) return false;

		final LogicalOpExtend oo = (LogicalOpExtend) o;
		return oo.extendExpressions.equals(extendExpressions);
	}

	@Override
	public int hashCode(){
		return extendExpressions.hashCode();
	}

	public VarExprList getExtendExpressions() {
		return extendExpressions;
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "> Extend ( " + extendExpressions.toString() + " )";
	}
}
