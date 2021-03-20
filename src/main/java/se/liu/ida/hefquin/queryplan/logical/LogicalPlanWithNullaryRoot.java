package se.liu.ida.hefquin.queryplan.logical;

import se.liu.ida.hefquin.queryplan.LogicalPlan;

public interface LogicalPlanWithNullaryRoot extends LogicalPlan
{
	@Override
	NullaryLogicalOp getRootOperator();

}
