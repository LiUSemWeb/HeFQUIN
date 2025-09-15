package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;

public interface PhysicalOpFactory
{
	/**
	 * Returns true if this factory can create a physical operator for the given
	 * logical operator and expected input variables.
	 * 
	 * @param lop       the logical operator to check
	 * @param inputVars expected input variables
	 * @return {@code true} if this factory can handle the inputs; {@code false}
	 *         otherwise
	 */
	boolean supports( LogicalOperator lop, ExpectedVariables inputVars );

	/**
	 * Creates a physical operator for the given logical operator.
	 *
	 * Precondition: call only if {@link #supports(...)} has returned true for the
	 * same logical operator and input variables.
	 *
	 * @param lop the logical operator
	 * @return a physical operator
	 * @throws IllegalArgumentException if the precondition was violated
	 */
	PhysicalOperator create( LogicalOperator lop );
}