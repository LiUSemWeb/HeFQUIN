package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

public interface LogicalPlanWithBinaryRoot extends LogicalPlan
{
	@Override
	BinaryLogicalOp getRootOperator();

	/**
	 * Convenience method that always should return the same as what is returned
	 * by calling {@link LogicalPlan#getSubPlan(int)} with a value of 0.
	 */
	LogicalPlan getSubPlan1();

	/**
	 * Convenience method that always should return the same as what is returned
	 * by calling {@link LogicalPlan#getSubPlan(int)} with a value of 1.
	 */
	LogicalPlan getSubPlan2();
}
