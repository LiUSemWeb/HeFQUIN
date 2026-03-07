package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.QueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

/**
 * This is the top-level interface for all types of physical operators of
 * HeFQUIN, where a physical operator is an element in a (physical) query
 * execution plan and is associated with a concrete algorithm that produces
 * a result (in HeFQUIN, this would be in the form of a sequence of solution
 * mappings) by consuming such results produced by the sub-plans under the
 * current operator.
 * <p>
 * The {@link PhysicalOperator#createExecOp(boolean, QueryPlanningInfo, ExpectedVariables...)}
 * function can be used to obtain an {@link ExecutableOperator} that provides
 * an implementation of the algorithm associated with the physical operator
 * in a form that can be plugged directly into the query execution framework
 * of HeFQUIN.
 */
public interface PhysicalOperator extends QueryPlanOperator
{
	/**
	 * Creates and returns the executable operator to be used for
	 * this physical operator. The implementation of this method
	 * has to create a new {@link ExecutableOperator} object each
	 * time it is called.
	 *
	 * The given {@link QueryPlanningInfo} object is passed to
	 * the created executable operator (to be available via the
	 * {@link ExecutableOperator#getQueryPlanningInfo()} method)
	 * and should be taken from the physical plan whose root
	 * operator is this physical operator.
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
	ExecutableOperator createExecOp( boolean collectExceptions,
	                                 QueryPlanningInfo qpInfo,
	                                 ExpectedVariables ... inputVars );

	void visit( PhysicalPlanVisitor visitor );
}
