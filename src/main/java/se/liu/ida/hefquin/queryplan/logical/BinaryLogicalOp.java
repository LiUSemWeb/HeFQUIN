package se.liu.ida.hefquin.queryplan.logical;

import se.liu.ida.hefquin.queryplan.LogicalOperator;

public interface BinaryLogicalOp extends LogicalOperator
{
	LogicalOperator getChildOp1();

	LogicalOperator getChildOp2();
}
