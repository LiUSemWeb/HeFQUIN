package se.liu.ida.hefquin.engine.queryplan.logical;

public interface LogicalOperator
{
	void visit( LogicalPlanVisitor visitor ); 
}
