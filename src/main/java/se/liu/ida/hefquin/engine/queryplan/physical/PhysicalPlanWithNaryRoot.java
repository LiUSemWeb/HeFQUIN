package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface PhysicalPlanWithNaryRoot extends PhysicalPlan
{
	@Override
	NaryPhysicalOp getRootOperator();
}
