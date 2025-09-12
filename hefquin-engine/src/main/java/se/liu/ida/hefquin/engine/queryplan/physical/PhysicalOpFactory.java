package se.liu.ida.hefquin.engine.queryplan.physical;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;

/**
 * Factory used to create physical operators ({@link PhysicalOperator}) from
 * logical operators ({@link LogicalOperator}) by consulting a registry of
 * providers ({@link PhysicalOpProvider}).
 *
 * Providers are queried in the order they were registered. The first provider
 * that supports the operator given the provided input variables is used to create
 * the physical operator.
 * 
 * All providers should be registered during initialization.
 *
 * If no registered provider supports the given inputs, the factory throws
 * {@link UnsupportedOperationException}.
 */

public class PhysicalOpFactory
{
	private final List<PhysicalOpProvider> providers = new ArrayList<>();

	/**
     * Registers a provider at the end of the lookup chain.
     *
     * @param provider the provider to add
     * @return this factory (for chaining)
     */
	public PhysicalOpFactory register( final PhysicalOpProvider provider ) {
		providers.add(provider);
		return this;
	}

	/**
     * Creates a physical operator for the given logical operator by
     * consulting registered providers in order, or throws {@link UnsupportedOperationException}
	 * if no registered provider supports the given inputs.
     *
     * @param lop        logical operator
     * @param inputVars  expected input variables
     * @return the operator produced by the first supporting provider
     * @throws UnsupportedOperationException if no provider supports the inputs
     */
	public PhysicalOperator create( final LogicalOperator lop, final ExpectedVariables inputVars ) {
		for ( final PhysicalOpProvider provider : providers ) {
			if ( provider.supports(lop, inputVars) ) {
				System.err.println(lop.getClass());
				System.err.println(provider.getClass());
				return provider.create(lop);
			}
		}
		throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}
}
