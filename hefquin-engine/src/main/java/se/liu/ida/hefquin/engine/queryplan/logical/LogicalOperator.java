package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlanOperator;

/**
 * The top-level interface for all types of logical operators of HeFQUIN.
 */
public interface LogicalOperator extends QueryPlanOperator
{
	void visit( LogicalPlanVisitor visitor );

	/**
	 * Indicates whether this logical operator can potentially reduce duplicate
	 * solution mappings as part of its normal operation.
	 * <p>
	 * If this method returns {@code true}, the operator has the capability
	 * to reduce duplicates; if it returns {@code false}, the operator does not
	 * reduce duplicates under any circumstances.
	 *
	 * @return {@code true} if the operator may reduce duplicates; {@code false} otherwise.
	 */
	boolean mayReduce();
}
