package se.liu.ida.hefquin.queryplan;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;

public interface LogicalOperator
{
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
	LogicalOperator getChild( int i ) throws NoSuchElementException;

	void visit( LogicalPlanVisitor visitor ); 
}
