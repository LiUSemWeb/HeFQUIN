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

	@Override
	public int hashCode() {
		return expr.hashCode() ^ attribute.hashCode();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return     o instanceof MappingOpExtend e
		       &&  e.attribute.equals(attribute)
		       &&  e.expr.equals(expr);
	}

	@Override
	public String toString() {
		return "extend(" + expr.toString() + ")";
	}
}
