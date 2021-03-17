package se.liu.ida.hefquin.queryplan;

import java.util.NoSuchElementException;

public interface PhysicalOperator
{
	/**
	 * Returns an {@link ExecutableOperatorCreator} that can create the
	 * {@link ExecutableOperator} to be used for this physical operator.
	 */
	ExecutableOperatorCreator getExecOpCreator();

	/**
	 * Returns the number of children that this operator has.
	 */
	int numberOfChildren();

	/**
	 * Returns the i-th child of this operator, where i starts at
	 * index 0 (zero).
	 *
	 * If the operator has fewer children (or no children at all),
	 * then a {@link NoSuchElementException} will be thrown.
	 */
	PhysicalOperator getChild( int i ) throws NoSuchElementException;
}
