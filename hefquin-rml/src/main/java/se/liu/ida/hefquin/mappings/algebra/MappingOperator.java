package se.liu.ida.hefquin.mappings.algebra;

import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;

public interface MappingOperator
{
	/**
	 * Returns an identifier of this operator, which should be distinct from
	 * the identifiers of all other operators within the same plan (no matter
	 * what type of operator they are).
	 */
	int getID();

	/**
	 * Returns the number of sub-expressions that a mapping expression is
	 * expected to have if it has this operator as its root operator. Hence,
	 * for nullary operators, this method returns 0; for unary operators, it
	 * returns 1; for binary operators, it returns 2. For n-ary operators
	 * (which can be applied to an arbitrary number of sub-expressions),
	 * this method returns {@link Integer#MAX_VALUE}.
	 */
	int getExpectedNumberOfSubExpressions();

	Set<String> getSchema();

	boolean isValid();

	boolean isValidInput( Map<SourceReference,DataObject> srMap );

	void visit( MappingOperatorVisitor visitor );

	MappingRelation evaluate( Map<SourceReference,DataObject> srMap );
}
