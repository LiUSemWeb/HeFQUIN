package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;

public interface PhysicalOperator
{
	/**
	 * Creates and returns the executable operator to be used for
	 * this physical operator. The implementation of this method
	 * has to create a new {@link ExecutableOperator} object each
	 * time it is called.
	 *
	 * The given collectExceptions flag is passed to the executable
	 * operator and determines whether that operator collects its
	 * exceptions (see {@link ExecutableOperator#getExceptionsCaughtDuringExecution()})
	 * or throws them immediately.
	 *
	 * The number of {@link ExpectedVariables} objects passed as
	 * arguments to this method must be in line with the degree of
	 * this operator (e.g., for a unary operator, exactly one such
	 * object must be passed).
	 */
	ExecutableOperator createExecOp( boolean collectExceptions, ExpectedVariables ... inputVars );

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

	void visit(final PhysicalPlanVisitor visitor);
	
	/**
	 * Returns an identifier of this operator, which should be unique for all the operators within the same plan.
	 */
	int getID();
}
