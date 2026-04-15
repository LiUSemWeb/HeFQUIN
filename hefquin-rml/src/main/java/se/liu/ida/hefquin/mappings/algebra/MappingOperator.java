package se.liu.ida.hefquin.mappings.algebra;

public interface MappingOperator
{
	/**
	 * Returns the number of sub-expressions that a mapping expression is
	 * expected to have if it has this operator as its root operator. Hence,
	 * for nullary operators, this method returns 0; for unary operators, it
	 * returns 1; for binary operators, it returns 2. For n-ary operators
	 * (which can be applied to an arbitrary number of sub-expressions),
	 * this method returns {@link Integer#MAX_VALUE}.
	 */
	int getExpectedNumberOfSubExpressions();

	void visit( MappingOperatorVisitor visitor );
}
