package se.liu.ida.hefquin.queryplan.logical;

import java.util.Iterator;

import se.liu.ida.hefquin.queryplan.LogicalPlan;

public interface LogicalPlanWithNaryRoot extends LogicalPlan
{
	@Override
	NaryLogicalOp getRootOperator();

	/**
	 * Convenience method that always should return an iterator over the same
	 * sub-plans that can be accessed via {@link LogicalPlan#getSubPlan(int)}.
	 */
	Iterator<LogicalPlan> getSubPlans();

}
