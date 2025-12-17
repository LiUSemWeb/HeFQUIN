package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;

public class LogicalOpFixedSolMap extends BaseForQueryPlanOperator
                                  implements NullaryLogicalOp
{
	protected final SolutionMapping sm;
	protected final ExpectedVariables expectedVars;

	public LogicalOpFixedSolMap( final SolutionMapping sm ) {
		assert sm!= null;

		this.sm = sm;

		final Set<Var> cVars = sm.asJenaBinding().varsMentioned();
		final Set<Var> pVars = Set.of();

		expectedVars = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return cVars; }
			@Override public Set<Var> getPossibleVariables() { return pVars; }
		};
	}

	public SolutionMapping getSolutionMapping() {
		return sm;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		if (    o instanceof LogicalOpFixedSolMap fin
		     && fin.getSolutionMapping().equals(sm) )
			return true;

		return false; 
	}

	@Override
	public int hashCode(){
		return sm.hashCode();
	}

	@Override
	public String toString(){
		return "sm" + " (" + getID() + ")"
		       + "\t " + sm.toString();
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 0;

		return expectedVars;
	}

}
