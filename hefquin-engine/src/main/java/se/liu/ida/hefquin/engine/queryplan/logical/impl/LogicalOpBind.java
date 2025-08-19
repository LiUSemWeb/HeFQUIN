package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpBind extends BaseForQueryPlanOperator implements UnaryLogicalOp
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

		final Set<Var> certainVars = new HashSet<>( expVarsInput.getCertainVariables() );
		final Set<Var> possibleVars = new HashSet<>( expVarsInput.getPossibleVariables() );

		// In general, the variable in a BIND clause is only possible,
		// not certain, because evaluating the expression of the BIND
		// clause may result in an error, in which case the BIND variable
		// remains unbound. Yet, for some expressions we can be sure that
		// their evaluation does not result in an error (e.g., constants);
		// hence, we check for such cases and add their BIND variable as
		// a certain one, rather than only a possible one.
		for ( final Var bindVar : bindExpressions.getVars() ) {
			final Expr expr = bindExpressions.getExpr(bindVar);
			if ( mightProduceError(expr, certainVars) )
				possibleVars.add(bindVar);
			else
				certainVars.add(bindVar);
		}

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	/**
	 * Returns <code>true</code> if it is <em>not</em> guaranteed that
	 * evaluating the given expression may result in an error.
	 */
	protected boolean mightProduceError( final Expr expr, final Set<Var> certainVars ) {
		if ( expr.isConstant() )
			return false;

		if ( expr.isVariable() && certainVars.contains(expr.asVar()) )
			return false;

		return true;
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