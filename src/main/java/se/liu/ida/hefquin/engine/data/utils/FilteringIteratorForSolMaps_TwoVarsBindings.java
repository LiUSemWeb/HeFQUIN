package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.utils.WrappingIterable;
import se.liu.ida.hefquin.engine.utils.WrappingIteratorFactory;

/**
 * This is an iterator of solution mappings that consumes another iterator
 * and passes on only the solution mappings that have given values for two
 * variables.
 */
public class FilteringIteratorForSolMaps_TwoVarsBindings extends FilteringIteratorForSolMapsBase
{
	public static WrappingIterable<SolutionMapping> createAsIterable( final Iterable<SolutionMapping> input,
	                                                                  final Var var1, final Node value1,
	                                                                  final Var var2, final Node value2 ) {
		return new WrappingIterable<SolutionMapping>(input, getFactory(var1, value1, var2, value2) );
	}


	protected final Var var1, var2;
	protected final Node value1, value2;

	public FilteringIteratorForSolMaps_TwoVarsBindings(
			final Iterator<SolutionMapping> input,
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
	{
		super(input);
		this.var1   = var1;
		this.var2   = var2;
		this.value1 = value1;
		this.value2 = value2;
	}

	public FilteringIteratorForSolMaps_TwoVarsBindings(
			final Iterable<SolutionMapping> input,
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
	{
		this( input.iterator(), var1, value1, var2, value2 );
	}

	@Override
	protected SolutionMapping applyFilter( final SolutionMapping sm ) {
		final Binding b = sm.asJenaBinding();
		final Node inputValue1 = b.get(var1);
		final Node inputValue2 = b.get(var2);

		if ( inputValue1 == null || inputValue1.equals(value1) ) {
			if ( inputValue2 == null || inputValue2.equals(value2) ) {
				return sm;
			}
		}

		return null;
	}


	public static WrappingIteratorFactory<SolutionMapping> getFactory( final Var var1, final Node value1,
	                                                                   final Var var2, final Node value2 ) {
		return new WrappingIteratorFactory<SolutionMapping>() {
			@Override
			public Iterator<SolutionMapping> createIterator(final Iterator<SolutionMapping> input) {
				return new FilteringIteratorForSolMaps_TwoVarsBindings(input, var1, value1, var2, value2);
			}
		};
	}

}
