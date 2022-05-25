package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class FilteringIteratorForSolMaps_ExprList implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input;
	protected final ExprList filterExpressions;

	protected SolutionMapping nextOutputElement = null;

	public FilteringIteratorForSolMaps_ExprList( final Iterator<SolutionMapping> input,
	                                             final ExprList filterExpressions ) {
		assert input != null;
		assert filterExpressions != null;

		this.input = input;
		this.filterExpressions = filterExpressions;
	}

	public FilteringIteratorForSolMaps_ExprList( final Iterable<SolutionMapping> input,
	                                             final ExprList filterExpressions ) {
		this( input.iterator(), filterExpressions );
	}

	@Override
	public boolean hasNext() {
		while ( nextOutputElement == null && input.hasNext() ) {
			nextOutputElement = applyFilter( input.next() );
		}

		return ( nextOutputElement != null );
	}

	@Override
	public SolutionMapping next() {
		if ( ! hasNext() )
			throw new NoSuchElementException();

		final SolutionMapping output = nextOutputElement;
		nextOutputElement = null;
		return output;
	}

	/**
	 * Returns the given solution mapping if it passes the filter condition.
	 * Returns <code>null</code> if the given solution mapping does not pass
	 * the filter condition.
	 */
	protected SolutionMapping applyFilter( final SolutionMapping sm ) {
		// Verify that the given solution mapping satisfies each of the filter expressions
		final Binding jsm = sm.asJenaBinding();
		for ( final Expr e : filterExpressions.getList() ) {
			try {
				final NodeValue evaluationResult = ExprUtils.eval(e, jsm);
				if( evaluationResult.equals(NodeValue.FALSE) ) {
					return null;
				}
				else if ( ! evaluationResult.equals(NodeValue.TRUE) ) {
					throw new IllegalArgumentException("The result of the eval is neither TRUE nor FALSE!");
				}
			}
			catch ( final VariableNotBoundException ex ) {
				// If evaluating the filter expression based on the given
				// solution mapping results in this error, then this solution
				// mapping does not satisfy the filter condition.
				return null;
			}
		}

		return sm;
	}

}
