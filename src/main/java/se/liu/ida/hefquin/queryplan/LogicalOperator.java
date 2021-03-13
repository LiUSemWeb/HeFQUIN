package se.liu.ida.hefquin.queryplan;

import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;

public interface LogicalOperator
{
	public void visit( final LogicalPlanVisitor visitor ); 
}
