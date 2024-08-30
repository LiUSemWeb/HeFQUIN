package se.liu.ida.hefquin.engine.queryplan.physical;

public interface PhysicalPlanWithNaryRoot extends PhysicalPlan
{
	@Override
	NaryPhysicalOp getRootOperator();
}
