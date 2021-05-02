package se.liu.ida.hefquin.engine.queryplan;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public interface LogicalOperator
{
	void visit( LogicalPlanVisitor visitor ); 
}
