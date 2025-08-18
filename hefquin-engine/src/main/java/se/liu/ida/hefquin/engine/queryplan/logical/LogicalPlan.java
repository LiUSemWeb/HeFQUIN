package se.liu.ida.hefquin.engine.queryplan.logical;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.info.GenericPlan;

public interface LogicalPlan extends GenericPlan
{
	/**
	 * Returns the root operator of this plan.
	 */
	LogicalOperator getRootOperator();

	@Override
	LogicalPlan getSubPlan( int i ) throws NoSuchElementException;
}
