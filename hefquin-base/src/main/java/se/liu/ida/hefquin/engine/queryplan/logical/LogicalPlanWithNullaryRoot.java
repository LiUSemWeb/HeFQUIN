package se.liu.ida.hefquin.engine.queryplan.logical;

public interface LogicalPlanWithNullaryRoot extends LogicalPlan
{
	@Override
	NullaryLogicalOp getRootOperator();

}
