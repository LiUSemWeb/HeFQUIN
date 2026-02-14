package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOpForLogicalOp;

/**
 * Base class for physical operators that implement
 * some form of a multi-way left join algorithm.
 */
public abstract class BaseForPhysicalOpMultiwayLeftJoin
		implements NaryPhysicalOpForLogicalOp
{
	protected final LogicalOpMultiwayLeftJoin lop;

	protected BaseForPhysicalOpMultiwayLeftJoin( final LogicalOpMultiwayLeftJoin lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public LogicalOpMultiwayLeftJoin getLogicalOperator() {
		return lop;
	}
}
