package se.liu.ida.hefquin.queryplan;

public interface PhysicalOperator
{
	/**
	 * Creates and returns the executable operator to be used for
	 * this physical operator. The implementation of this method
	 * has to create a new {@link ExecutableOperator} object each
	 * time it is called.
	 */
	ExecutableOperator createExecOp();

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
}
