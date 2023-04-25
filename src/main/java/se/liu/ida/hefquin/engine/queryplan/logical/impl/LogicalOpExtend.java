package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import org.apache.jena.sparql.core.VarExprList;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpExtend implements UnaryLogicalOp
{
	protected final VarExprList extendExpressions;

	public LogicalOpExtend(final VarExprList extendExpressions ) {
		assert extendExpressions != null;
		assert ! extendExpressions.isEmpty();

		this.extendExpressions = extendExpressions;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		return inputVars[0];
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
