package se.liu.ida.hefquin.engine.queryplan.logical;

public interface LogicalPlanWithUnaryRoot extends LogicalPlan
{
	@Override
	UnaryLogicalOp getRootOperator();

	/**
	 * Convenience method that always should return the same as what is returned
	 * by calling {@link LogicalPlan#getSubPlan(int)} with a value of 0.
	 */
	LogicalPlan getSubPlan();

}
