package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.List;

import org.apache.jena.query.SortCondition;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpOrderBy extends BaseForLogicalOps implements UnaryLogicalOp
{
	protected final List<SortCondition> sortConditions;

	public LogicalOpOrderBy( final List<SortCondition> sortConditions, final boolean mayReduce ) {
		super(mayReduce);

		assert sortConditions != null;

		this.sortConditions = sortConditions;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		return inputVars[0];
	}

	public List<SortCondition> getSortConditions() {
		return sortConditions;
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return    o instanceof LogicalOpOrderBy oo
		       && oo.sortConditions.equals(sortConditions)
		       && oo.mayReduce == mayReduce;
	}

	@Override
	public int hashCode(){
		return getClass().hashCode() ^ sortConditions.hashCode();
	}

	@Override
	public String toString() {
		return "order by ( " + sortConditions.toString() + " )";
	}

}
