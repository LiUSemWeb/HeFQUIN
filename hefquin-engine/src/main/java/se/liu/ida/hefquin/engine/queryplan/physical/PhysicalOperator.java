package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;

/**
 * This is the top-level interface for all types of physical operators of
 * HeFQUIN, where a physical operator is an element in a (physical) query
 * execution plan and is associated with a concrete algorithm that produces
 * a result (in HeFQUIN, this would be in the form of a sequence of solution
 * mappings) by consuming such results produced by the sub-plans under the
 * current operator.
 *
 * The {@link PhysicalOperator#createExecOp(boolean, ExpectedVariables...)
 * function can be used to obtain an {@link ExecutableOperator} that provides
 * an implementation of the algorithm associated with the physical operator
 * in a form that can be plugged directly into the query execution framework
 * of HeFQUIN.
 */
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

	/**
	 * Returns an identifier of this operator, which should be unique
	 * for all the operators within the same plan (no matter what type
	 * of operator they are).
	 */
	int getID();

	void visit( PhysicalPlanVisitor visitor );
}
