package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLeftJoin;
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
	protected final boolean useOuterJoinSemantics;
	protected final boolean mayReduce;

	protected BaseForPhysicalOpBinaryJoin( final boolean useOuterJoinSemantics, final boolean mayReduce ) {
		this.useOuterJoinSemantics = useOuterJoinSemantics;
		this.mayReduce = mayReduce;
	}

	@Override
	public BinaryLogicalOp getLogicalOperator() {
		if ( useOuterJoinSemantics )
			return LogicalOpLeftJoin.getInstance();
		else
			return LogicalOpJoin.getInstance();
	}
}
