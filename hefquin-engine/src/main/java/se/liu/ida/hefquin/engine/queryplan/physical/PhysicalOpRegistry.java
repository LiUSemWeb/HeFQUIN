package se.liu.ida.hefquin.engine.queryplan.physical;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;

/**
 * Class used to create physical operators ({@link PhysicalOperator}) from
 * logical operators ({@link LogicalOperator}) by consulting a registry of
 * factories ({@link PhysicalOpFactory}).
 *
 * Factories are queried in the order they were registered. The first factory
 * that supports the operator given the provided input variables is used to create
 * the physical operator.
 * 
 * All factories should be registered during initialization.
 *
 * If no registered factory supports the given inputs, the registry throws
 * {@link UnsupportedOperationException}.
 */
public class PhysicalOpRegistry
{
	private final List<PhysicalOpFactory> factories = new ArrayList<>();

	/**
     * Registers a factory at the end of the lookup chain.
     *
     * @param factory the factory to add
     * @return this registry (for chaining)
     */
	public PhysicalOpRegistry register( final PhysicalOpFactory factory ) {
		factories.add(factory);
		return this;
	}

	/**
     * Creates a physical operator for the given logical operator by
     * consulting registered factories in order, or throws {@link UnsupportedOperationException}
	 * if no registered factory supports the given inputs.
     *
     * @param lop        logical operator
     * @param inputVars  expected input variables
     * @return the operator produced by the first supporting factory
     * @throws NoSuchElementException if no factory supports the inputs
     */
	public PhysicalOperator create( final LogicalOperator lop, final ExpectedVariables inputVars ) {
		for ( final PhysicalOpFactory factory : factories ) {
			if ( factory.supports(lop, inputVars) ) {
				return factory.create(lop);
			}
		}
		throw new NoSuchElementException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}
}
