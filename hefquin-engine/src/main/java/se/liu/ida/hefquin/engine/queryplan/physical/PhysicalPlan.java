package se.liu.ida.hefquin.engine.queryplan.physical;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlan;

public interface PhysicalPlan extends QueryPlan
{
	/**
	 * Returns the root operator of this plan.
	 */
	PhysicalOperator getRootOperator();

	@Override
	PhysicalPlan getSubPlan( int i ) throws NoSuchElementException;
}
