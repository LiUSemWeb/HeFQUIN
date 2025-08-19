package se.liu.ida.hefquin.engine.queryplan.logical;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlan;

public interface LogicalPlan extends QueryPlan
{
	/**
	 * Returns the root operator of this plan.
	 */
	LogicalOperator getRootOperator();

	@Override
	LogicalPlan getSubPlan( int i ) throws NoSuchElementException;
}
