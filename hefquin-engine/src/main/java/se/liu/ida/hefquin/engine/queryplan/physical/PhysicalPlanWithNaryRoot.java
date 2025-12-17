package se.liu.ida.hefquin.engine.queryplan.physical;

import java.util.Iterator;

public interface PhysicalPlanWithNaryRoot extends PhysicalPlan
{
	@Override
	NaryPhysicalOp getRootOperator();

	/**
	 * Convenience method that always should return an iterator over the same
	 * sub-plans that can be accessed via {@link PhysicalPlan#getSubPlan(int)}.
	 */
	Iterator<PhysicalPlan> getSubPlans();
}
