package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;

/**
 * Base class for physical operators that implement some form of a
 * binary join algorithm; i.e., these algorithms consume two sequences
 * of input solution mappings (produced by the two sub-plans under this
 * operator) and join these the solution mappings from these two sequences
 * locally (i.e., within in the engine rather than by interacting with any
 * federation member).
 */
public abstract class BaseForPhysicalOpBinaryJoin
		implements BinaryPhysicalOpForLogicalOp
{
	@Override
	public LogicalOpJoin getLogicalOperator() {
		return LogicalOpJoin.getInstance();
	}
}
