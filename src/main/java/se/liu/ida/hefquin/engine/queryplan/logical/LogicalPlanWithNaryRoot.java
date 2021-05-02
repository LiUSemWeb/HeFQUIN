package se.liu.ida.hefquin.engine.queryplan.logical;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

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
