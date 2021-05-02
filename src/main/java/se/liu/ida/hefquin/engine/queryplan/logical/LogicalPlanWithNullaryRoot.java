package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

public interface LogicalPlanWithNullaryRoot extends LogicalPlan
{
	@Override
	NullaryLogicalOp getRootOperator();

}
