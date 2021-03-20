package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.PhysicalPlan;

public interface PhysicalPlanWithUnaryRoot extends PhysicalPlan
{
	@Override
	UnaryPhysicalOp getRootOperator();

	/**
	 * Convenience method that always should return the same as what is returned
	 * by calling {@link PhysicalPlan#getSubPlan(int)} with a value of 0.
	 */
	PhysicalPlan getSubPlan();
}
