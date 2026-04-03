package se.liu.ida.hefquin.mappings.algebra.ops;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;

public abstract class BaseForUnaryMappingOperator extends BaseForMappingOperator
{
	protected final MappingOperator subOp;

	protected BaseForUnaryMappingOperator( final MappingOperator subOp ) {
		assert subOp != null;
		this.subOp = subOp;
	}

	@Override
	public int getExpectedNumberOfSubExpressions() { return 1; }

	public MappingOperator getSubOp() {
		return subOp;
	}
}
