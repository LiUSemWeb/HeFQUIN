package se.liu.ida.hefquin.mappings.algebra.ops;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;

public abstract class BaseForBinaryMappingOperator extends BaseForMappingOperator
{
	protected final MappingOperator subOp1;
	protected final MappingOperator subOp2;

	protected BaseForBinaryMappingOperator( final MappingOperator subOp1,
	                                        final MappingOperator subOp2 ) {
		assert subOp1 != null;
		assert subOp2 != null;

		this.subOp1 = subOp1;
		this.subOp2 = subOp2;
	}

	public MappingOperator getSubOp1() {
		return subOp1;
	}

	public MappingOperator getSubOp2() {
		return subOp2;
	}
}
