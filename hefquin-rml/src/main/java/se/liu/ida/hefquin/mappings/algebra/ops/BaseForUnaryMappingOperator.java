package se.liu.ida.hefquin.mappings.algebra.ops;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;

public abstract class BaseForUnaryMappingOperator extends BaseForMappingOperator
{
	protected final MappingOperator subOp;

	protected BaseForUnaryMappingOperator( final MappingOperator subOp ) {
		assert subOp != null;
		this.subOp = subOp;
	}

	public MappingOperator getSubOp() {
		return subOp;
	}
}
