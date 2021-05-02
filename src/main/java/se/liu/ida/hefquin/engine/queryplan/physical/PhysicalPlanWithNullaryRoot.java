package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface PhysicalPlanWithNullaryRoot extends PhysicalPlan
{
	@Override
	NullaryPhysicalOp getRootOperator();

}
