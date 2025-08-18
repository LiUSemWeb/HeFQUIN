package se.liu.ida.hefquin.engine.queryplan.physical;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.info.GenericPlan;

public interface PhysicalPlan extends GenericPlan
{
	/**
	 * Returns the root operator of this plan.
	 */
	PhysicalOperator getRootOperator();

	@Override
	PhysicalPlan getSubPlan( int i ) throws NoSuchElementException;
}
