package se.liu.ida.hefquin.engine.queryplan.physical;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

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
		factories.add( factory );
		return this;
	}

	/**
	 * Creates a physical operator for the given logical operator by consulting
	 * registered factories in order. If no registered factory supports the
	 * given inputs, an {@link UnsupportedOperationException} is thrown.
	 *
	 * @param lop       logical operator
	 * @param inputVars expected input variables
	 * @return the operator produced by the first supporting factory
	 * @throws NoSuchElementException if no factory supports the inputs
	 */
	public NullaryPhysicalOp create( final NullaryLogicalOp lop ) {
		// find the factory that supports the given input and use this factory
		for ( final PhysicalOpFactory factory : factories ) {
			if ( factory.supports(lop) ) {
				return factory.create(lop);
			}
		}

		// no supporting factory found
		throw new NoSuchElementException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	/**
	 * Creates a physical operator for the given logical operator by consulting
	 * registered factories in order. If no registered factory supports the
	 * given inputs, an {@link UnsupportedOperationException} is thrown.
	 *
	 * @param lop       logical operator
	 * @param inputVars expected input variables
	 * @return the operator produced by the first supporting factory
	 * @throws NoSuchElementException if no factory supports the inputs
	 */
	public UnaryPhysicalOp create( final UnaryLogicalOp lop, final ExpectedVariables inputVars ) {
		// find the factory that supports the given input and use this factory
		for ( final PhysicalOpFactory factory : factories ) {
			if ( factory.supports(lop, inputVars) ) {
				return factory.create(lop);
			}
		}

		// no supporting factory found
		throw new NoSuchElementException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	/**
	 * Creates a physical operator for the given logical operator by consulting
	 * registered factories in order. If no registered factory supports the
	 * given inputs, an {@link UnsupportedOperationException} is thrown.
	 *
	 * @param lop       logical operator
	 * @param inputVars expected input variables
	 * @return the operator produced by the first supporting factory
	 * @throws NoSuchElementException if no factory supports the inputs
	 */
	public BinaryPhysicalOp create( final BinaryLogicalOp lop,
	                                final ExpectedVariables inputVars1,
	                                final ExpectedVariables inputVars2 ) {
		// find the factory that supports the given input and use this factory
		for ( final PhysicalOpFactory factory : factories ) {
			if ( factory.supports(lop, inputVars1, inputVars2) ) {
				return factory.create(lop);
			}
		}

		// no supporting factory found
		throw new NoSuchElementException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	/**
	 * Creates a physical operator for the given logical operator by consulting
	 * registered factories in order. If no registered factory supports the
	 * given inputs, an {@link UnsupportedOperationException} is thrown.
	 *
	 * @param lop       logical operator
	 * @param inputVars expected input variables
	 * @return the operator produced by the first supporting factory
	 * @throws NoSuchElementException if no factory supports the inputs
	 */
	public NaryPhysicalOp create( final NaryLogicalOp lop, final ExpectedVariables ... inputVars ) {
		// find the factory that supports the given input and use this factory
		for ( final PhysicalOpFactory factory : factories ) {
			if ( factory.supports(lop, inputVars) ) {
				return factory.create(lop);
			}
		}

		// no supporting factory found
		throw new NoSuchElementException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}
}
