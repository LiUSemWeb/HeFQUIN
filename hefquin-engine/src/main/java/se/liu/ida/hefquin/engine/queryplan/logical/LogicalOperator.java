package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlanOperator;

/**
 * The top-level interface for all types of logical operators of HeFQUIN.
 */
public interface LogicalOperator extends QueryPlanOperator
{
	void visit( LogicalPlanVisitor visitor );

	/**
	 * Indicates whether this logical operator is permitted (and potentially
	 * encouraged) to reduce duplicate solution mappings as part of its execution.
	 * <p>
	 * If this method returns {@code true}, the operator may remove duplicate
	 * solution mappings if it has the capability to do so. If it returns
	 * {@code false}, the operator must preserve duplicates, even if it would
	 * otherwise be able to eliminate them.
	 *
	 * @return {@code true} if the operator may reduce duplicates; {@code false} otherwise.
	 */
	boolean mayReduce();
}
