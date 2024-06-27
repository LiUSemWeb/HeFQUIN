package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

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

	void visit( LogicalPlanVisitor visitor ); 
	
	int getID();
}