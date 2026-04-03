package se.liu.ida.hefquin.mappings.algebra.exprs;

import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;

/**
 * This is an abstract base class for classes that implement the
 * {@link MappingExpression} interface. This base class implements
 * {@link MappingExpression#getID()}.
 */
public class MappingExpressionImpl implements MappingExpression
{
	private static int counter = 0;

	protected final int id;
	protected final MappingOperator rootOp;
	protected final List<MappingExpression> subExprs;

	public MappingExpressionImpl( final MappingOperator rootOp,
	                              final MappingExpression ... subExprs ) {
		id = ++counter;
		this.rootOp = rootOp;

		// Check that the number of sub-expressions
		// is correct for the given root operator.
		if ( rootOp.getExpectedNumberOfSubExpressions() < Integer.MAX_VALUE ) {
			if ( rootOp.getExpectedNumberOfSubExpressions() != subExprs.length ) {
				throw new IllegalArgumentException("Unexpected number (" + subExprs.length + ") of sub-expressions for the given mapping operator, which is of type " + rootOp.getClass().getName() );
			}
		}

		if ( subExprs.length == 0 )
			this.subExprs = null;
		else
			this.subExprs = Arrays.asList(subExprs);
	}

	@Override
	public int getID() { return id; }

	@Override
	public MappingOperator getRootOperator() { return rootOp; }

	@Override
	public int numberOfSubExpressions() {
		if ( subExprs == null )
			return 0;
		else
			return subExprs.size();
	}

	@Override
	public MappingExpression getSubExpression( final int i ) {
		if ( subExprs == null )
			throw new IndexOutOfBoundsException();
		else
			return subExprs.get(i);
	}

	@Override
	public boolean equals( final Object o ) {
		// Since every expression has a unique ID, two different Java
		// objects that represent expressions cannot be equal even if
		// the expressions that they represent are identical (except
		// for their IDs).
		return ( o == this );
	}

	@Override
	public int hashCode(){
		return id;
	}

}
