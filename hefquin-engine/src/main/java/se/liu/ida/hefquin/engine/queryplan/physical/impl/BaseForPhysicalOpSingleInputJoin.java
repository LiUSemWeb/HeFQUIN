package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

/**
 * Base class for physical operators that implement some form of a join
 * algorithm that consumes only one input and produces the other input
 * as part of the algorithm itself.
 */
public abstract class BaseForPhysicalOpSingleInputJoin
		implements UnaryPhysicalOpForLogicalOp
{
	protected final UnaryLogicalOp lop;

	/**
	 * The given logical operator is expected to be either a
	 * {@link LogicalOpGPAdd} or a {@link LogicalOpGPOptAdd}.
	 */
	protected BaseForPhysicalOpSingleInputJoin( final UnaryLogicalOp lop ) {
		assert lop != null;
		assert (lop instanceof LogicalOpGPAdd) || (lop instanceof LogicalOpGPOptAdd);
		this.lop = lop;
	}

	@Override
	public UnaryLogicalOp getLogicalOperator() { return lop; }
}
