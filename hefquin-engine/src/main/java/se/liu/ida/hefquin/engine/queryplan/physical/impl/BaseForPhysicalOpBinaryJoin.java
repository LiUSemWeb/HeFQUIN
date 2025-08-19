package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;

import java.util.Objects;

/**
 * Base class for physical operators that implement some form of a
 * binary join algorithm; i.e., these algorithms consume two sequences
 * of input solution mappings (produced by the two sub-plans under this
 * operator) and join these the solution mappings from these two sequences
 * locally (i.e., within in the engine rather than by interacting with any
 * federation member).
 */
public abstract class BaseForPhysicalOpBinaryJoin extends BaseForQueryPlanOperator
                                                  implements BinaryPhysicalOpForLogicalOp
{
	protected final LogicalOpJoin lop;

	protected BaseForPhysicalOpBinaryJoin( final LogicalOpJoin lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof BinaryPhysicalOpForLogicalOp
				&& ((BinaryPhysicalOpForLogicalOp) o).getLogicalOperator().equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode() ^ Objects.hash( this.getClass().getName() );
	}

	@Override
	public LogicalOpJoin getLogicalOperator() {
		return lop;
	}

}
