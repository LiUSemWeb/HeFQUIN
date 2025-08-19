package se.liu.ida.hefquin.engine.queryplan.base;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;

/**
 * This interface captures properties and functionality that is common both
 * to logical and to physical operators.
 * <p>
 * This interface serves purely an abstract purpose in the sense that it is
 * not meant to be instantiated directly. Instead, {@link LogicalOperator}
 * and {@link PhysicalOperator}) are the relevant specializations of this
 * interfaces that are meant to be used throughout the code base.
 */
public interface QueryPlanOperator
{
	/**
	 * Returns an identifier of this operator, which should be distinct from
	 * the identifiers of all other operators within the same plan (no matter
	 * what type of operator they are).
	 */
	int getID();

	/**
	 * Returns the variables that can be expected in the solution
	 * mappings produced by this operator in the case that the input(s)
	 * to this operator contain solutions mappings with the given set(s)
	 * of variables. The number of {@link ExpectedVariables} objects
	 * passed to this method must be in line with the degree of this
	 * operator (e.g., for a unary operator, exactly one such object
	 * must be passed).
	 */
	ExpectedVariables getExpectedVariables( ExpectedVariables ... inputVars );
}
