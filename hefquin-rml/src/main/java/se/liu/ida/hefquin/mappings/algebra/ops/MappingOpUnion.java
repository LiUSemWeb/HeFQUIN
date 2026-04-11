package se.liu.ida.hefquin.mappings.algebra.ops;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;

public class MappingOpUnion implements MappingOperator
{
	protected final static MappingOpUnion instance = new MappingOpUnion();

	public static MappingOpUnion getInstance() { return instance; }

	protected MappingOpUnion() {}

	@Override
	public int getExpectedNumberOfSubExpressions() { return Integer.MAX_VALUE; }

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}
}
