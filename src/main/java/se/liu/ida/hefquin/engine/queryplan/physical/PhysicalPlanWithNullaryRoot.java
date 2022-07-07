package se.liu.ida.hefquin.engine.queryplan.physical;

public interface PhysicalPlanWithNullaryRoot extends PhysicalPlan
{
	@Override
	NullaryPhysicalOp getRootOperator();

}
