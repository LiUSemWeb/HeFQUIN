package se.liu.ida.hefquin.mappings.algebra.ops;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExpression;

public class MappingOpExtend implements MappingOperator
{
	protected final ExtendExpression expr;
	protected final String attribute;

	public MappingOpExtend( final ExtendExpression expr,
	                        final String attribute ) {
		assert expr != null;
		assert attribute != null;

		this.expr = expr;
		this.attribute = attribute;
	}

	public String getAttribute() { return attribute; }

	public ExtendExpression getExtendExpression() { return expr; }

	@Override
	public int getExpectedNumberOfSubExpressions() { return 1; }

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}
}
