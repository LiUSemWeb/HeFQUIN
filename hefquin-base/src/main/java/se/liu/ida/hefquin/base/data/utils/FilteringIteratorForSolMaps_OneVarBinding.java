package se.liu.ida.hefquin.base.data.utils;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.utils.WrappingIterable;
import se.liu.ida.hefquin.base.utils.WrappingIteratorFactory;

/**
 * This is an iterator of solution mappings that consumes another iterator
 * and passes on only the solution mappings that have a given value for a
 * given variable.
 */
public class FilteringIteratorForSolMaps_OneVarBinding extends FilteringIteratorForSolMapsBase
{
	public static WrappingIterable<SolutionMapping> createAsIterable( final Iterable<SolutionMapping> input,
	                                                                  final Var var,
	                                                                  final Node value ) {
		return new WrappingIterable<SolutionMapping>(input, getFactory(var, value) );
	}


	protected final Var var;
	protected final Node value;

	public FilteringIteratorForSolMaps_OneVarBinding( final Iterator<SolutionMapping> input, final Var var, final Node value ) {
		super(input);
		this.var   = var;
		this.value = value;
	}

	public FilteringIteratorForSolMaps_OneVarBinding( final Iterable<SolutionMapping> input, final Var var, final Node value ) {
		this( input.iterator(), var, value );
	}

	@Override
	protected SolutionMapping applyFilter( final SolutionMapping sm ) {
		final Node inputValue = sm.asJenaBinding().get(var);
		if ( inputValue == null || inputValue.equals(value) ) {
			return sm;
		}

		return null;
	}


	public static WrappingIteratorFactory<SolutionMapping> getFactory( final Var var, final Node value ) {
		return new WrappingIteratorFactory<SolutionMapping>() {
			@Override
			public Iterator<SolutionMapping> createIterator(final Iterator<SolutionMapping> input) {
				return new FilteringIteratorForSolMaps_OneVarBinding(input, var, value);
			}
		};
	}

}
