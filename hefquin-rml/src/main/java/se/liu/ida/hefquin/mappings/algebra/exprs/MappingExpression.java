package se.liu.ida.hefquin.mappings.algebra.exprs;

import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;

/**
 * This interface represents an expression of the mapping algebra introduced
 * in the following research paper. Every such expression has a root operator
 * and, depending on the type of root operator, a number of sub-expressions.
 * <p>
 * Sitt Min Oo and Olaf Hartig: "An Algebraic Foundation for Knowledge Graph
 * Construction." In Proceedings of the 22nd Extended Semantic Web Conference
 * (ESWC), 2025.
 */
public interface MappingExpression
{
	/**
	 * Returns an identifier of this mapping expression, which should be
	 * distinct from the identifiers of all other sub-expressions within
	 * the same expression.
	 */
	int getID();

	/**
	 * Returns {@code true} if this mapping expression is valid.
	 */
	boolean isValid();

	/**
	 * Returns the schema of the mapping relation that will
	 * be the result of evaluating this mapping expression.
	 */
	Set<String> getSchema();

	/**
	 * Returns the root operator of this expression.
	 */
	MappingOperator getRootOperator();

	/**
	 * Returns the number of sub-expressions that this expression has
	 * (considering sub-expressions that are direct children of the
	 * root operator of this expressions).
	 */
	int numberOfSubExpressions();

	/**
	 * Returns the i-th sub-expression of this expression, where i starts
	 * at index 0 (zero).
	 *
	 * If the expression has fewer sub-expressions (or no sub-expressions
	 * at all), then an {@link IndexOutOfBoundsException} will be thrown.
	 */
	MappingExpression getSubExpression( int i ) throws IndexOutOfBoundsException;

	/**
	 * Returns {@code true} if this expression is the same expression as
	 * the given one. Expressions are considered the same if they have
	 * the same root operator, the same number of sub-expressions, and
	 * the sub-expressions at every index are the same as well.
	 * <p>
	 * Notice that the {@link #equals(Object)} function cannot be used
	 * for the type of comparison provided by this function because
	 * {@link #equals(Object)} takes the IDs of the plans into account
	 * (which essentially means that {@link #equals(Object)} falls back
	 * to doing a {@code ==} comparison, because the IDs are unique).
	 */
	default boolean isSameExpression( final MappingExpression other ) {
		if ( this.equals(other) )
			return true;

		if ( numberOfSubExpressions() != other.numberOfSubExpressions() )
			return false;

		if ( ! getRootOperator().equals(other.getRootOperator()) )
			return false;

		for ( int i = 0; i < numberOfSubExpressions(); i++ ) {
			if ( ! getSubExpression(i).isSameExpression(other.getSubExpression(i)) )
				return false;
		}

		return true;
	}
}
