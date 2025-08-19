package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlanOperator;

/**
 * The top-level interface for all types of logical operators of HeFQUIN.
 */
public interface LogicalOperator extends QueryPlanOperator
{
	void visit( LogicalPlanVisitor visitor );
}
