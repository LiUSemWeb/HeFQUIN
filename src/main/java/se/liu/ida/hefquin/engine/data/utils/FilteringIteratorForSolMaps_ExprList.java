package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class FilteringIteratorForSolMaps_ExprList extends FilteringIteratorForSolMapsBase
{
	protected final ExprList filterExpressions;

	public FilteringIteratorForSolMaps_ExprList( final Iterator<SolutionMapping> input,
	                                             final ExprList filterExpressions ) {
		super(input);

		assert filterExpressions != null;
		assert ! filterExpressions.isEmpty();

		this.filterExpressions = filterExpressions;
	}

	public FilteringIteratorForSolMaps_ExprList( final Iterator<SolutionMapping> input,
	                                             final Expr... filterExpressions ) {
		this( input, createExprList(filterExpressions) );
	}

	public FilteringIteratorForSolMaps_ExprList( final Iterable<SolutionMapping> input,
	                                             final ExprList filterExpressions ) {
		this( input.iterator(), filterExpressions );
	}

	public FilteringIteratorForSolMaps_ExprList( final Iterable<SolutionMapping> input,
	                                             final Expr... filterExpressions ) {
		this( input, createExprList(filterExpressions) );
	}

	protected static ExprList createExprList( final Expr... filterExpressions ) {
		assert filterExpressions.length > 0;

		final ExprList l = new ExprList();
		for ( int i = 0; i < filterExpressions.length; ++i ) {
			assert filterExpressions[i] != null;
			l.add( filterExpressions[i] );
		}
		return l;
	}


	@Override
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
