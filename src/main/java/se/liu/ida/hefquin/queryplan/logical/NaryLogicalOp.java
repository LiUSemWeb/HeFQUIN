package se.liu.ida.hefquin.queryplan.logical;

import java.util.List;

import se.liu.ida.hefquin.queryplan.LogicalOperator;

public interface NaryLogicalOp extends LogicalOperator
{
	List<LogicalOperator> getChildren();
}
