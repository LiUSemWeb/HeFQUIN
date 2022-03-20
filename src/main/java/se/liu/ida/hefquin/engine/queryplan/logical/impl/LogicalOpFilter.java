package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import org.apache.jena.sparql.expr.Expr;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpFilter implements UnaryLogicalOp
{
	protected final Expr filterExpression;

	public LogicalOpFilter( final Expr filterExpression) {
		assert filterExpression != null;

		this.filterExpression = filterExpression;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof LogicalOpTPAdd) )
			return false;

		final LogicalOpFilter oo = (LogicalOpFilter) o;
		if ( oo == this )
			return true;
		else
			return oo.filterExpression.equals(filterExpression); 
	}

	public Expr getFilterExpression() {
		return filterExpression;
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){ // Unsure about what to do with this one.
		final int codeOfExpr = filterExpression.toString().hashCode();

		return "> filter" +
				"[" + codeOfExpr + "]"+
				" ( "
				+ filterExpression.getVarName()
				+ " )";
	}
}