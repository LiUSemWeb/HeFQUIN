package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.PhysicalPlan;

public interface PhysicalPlanWithNullaryRoot extends PhysicalPlan
{
	@Override
	NullaryPhysicalOp getRootOperator();

}
