package se.liu.ida.hefquin.mappings.algebra.ops;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;

/**
 * This is an abstract base class for classes that implement concrete
 * specializations of the {@link MappingOperator} interface. This base
 * class handles the creation of a unique ID per operator.
 */
public abstract class BaseForMappingOperator implements MappingOperator
{
	private static int counter = 0;

	protected final int id;

	protected BaseForMappingOperator() { id = ++counter; }

	@Override public int getID() { return id; }

}
