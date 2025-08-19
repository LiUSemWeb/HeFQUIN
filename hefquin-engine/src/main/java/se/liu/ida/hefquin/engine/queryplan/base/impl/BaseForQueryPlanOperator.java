package se.liu.ida.hefquin.engine.queryplan.base.impl;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlanOperator;

/**
 * This is an abstract base class for classes that implement concrete
 * specializations (sub-interfaces) of the {@link QueryPlanOperator}
 * interface. This base class handles the creation of a unique ID per
 * operator.
 */
public abstract class BaseForQueryPlanOperator implements QueryPlanOperator
{
	private static int counter = 0;

	protected final int id;

	protected BaseForQueryPlanOperator() { id = ++counter; }

	@Override public int getID() { return id; }
}
