package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.List;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpProject implements UnaryLogicalOp
{
	protected final List<Var> variables;

	public LogicalOpProject( final List<Var> variables ) {
		assert variables != null;
		assert ! variables.isEmpty();

		this.variables = variables;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		return inputVars[0];
	}

	public List<Var> getVariables() {
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
		return "Variables ( " + variables.toString() + " )";
	}

}
