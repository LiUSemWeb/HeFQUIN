package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.base.query.ExpectedVariables;

/**
 * The top-level interface for all types of logical operators of HeFQUIN.
 */
public interface LogicalOperator
{
	/**
	 * Returns the variables that can be expected in the solution
	 * mappings produced by this operator if the input(s) to this
	 * operator contain solutions mappings with the given set(s) of
	 * variables. The number of {@link ExpectedVariables} objects
	 * passed to this method must be in line with the degree of this
	 * operator (e.g., for a unary operator, exactly one such object
	 * must be passed).
	 */
	ExpectedVariables getExpectedVariables( ExpectedVariables ... inputVars );

	/**
	 * Returns an identifier of this operator, which should be unique
	 * for all the operators within the same plan (no matter what type
	 * of operator they are).
	 */
	int getID();

	void visit( LogicalPlanVisitor visitor );
}
