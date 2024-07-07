package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;

/**
 * Top-level base class for all implementations of {@link PhysicalOperator}.
 *
 * This base class handles the creation of a unique ID per operator.
 */
public abstract class BaseForPhysicalOps implements PhysicalOperator
{
	private static int counter = 0;

	protected final int id;

	public BaseForPhysicalOps() { id = ++counter; }

	@Override public int getID() { return id; }
}
