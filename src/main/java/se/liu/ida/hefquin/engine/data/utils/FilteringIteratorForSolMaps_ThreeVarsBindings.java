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
 * and passes on only the solution mappings that have given values for three
 * variables.
 */
public class FilteringIteratorForSolMaps_ThreeVarsBindings extends FilteringIteratorForSolMapsBase
{
	public static WrappingIterable<SolutionMapping> createAsIterable( final Iterable<SolutionMapping> input,
	                                                                  final Var var1, final Node value1,
	                                                                  final Var var2, final Node value2,
	                                                                  final Var var3, final Node value3 ) {
		return new WrappingIterable<SolutionMapping>(input, getFactory(var1, value1, var2, value2, var3, value3) );
	}


	protected final Var var1, var2, var3;
	protected final Node value1, value2, value3;

	public FilteringIteratorForSolMaps_ThreeVarsBindings(
			final Iterator<SolutionMapping> input,
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
	{
		super(input);
		this.var1   = var1;
		this.var2   = var2;
		this.var3   = var3;
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
	}

	public FilteringIteratorForSolMaps_ThreeVarsBindings(
			final Iterable<SolutionMapping> input,
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
	{
		this( input.iterator(), var1, value1, var2, value2, var3, value3 );
	}

	@Override
	protected SolutionMapping applyFilter( final SolutionMapping sm ) {
		final Binding b = sm.asJenaBinding();
		final Node inputValue1 = b.get(var1);
		final Node inputValue2 = b.get(var2);
		final Node inputValue3 = b.get(var3);

		if ( inputValue1 == null || inputValue1.equals(value1) ) {
			if ( inputValue2 == null || inputValue2.equals(value2) ) {
				if ( inputValue3 == null || inputValue3.equals(value3) ) {
					return sm;
				}
			}
		}

		return null;
	}


	public static WrappingIteratorFactory<SolutionMapping> getFactory( final Var var1, final Node value1,
	                                                                   final Var var2, final Node value2,
	                                                                   final Var var3, final Node value3 ) {
		return new WrappingIteratorFactory<SolutionMapping>() {
			@Override
			public Iterator<SolutionMapping> createIterator(final Iterator<SolutionMapping> input) {
				return new FilteringIteratorForSolMaps_ThreeVarsBindings(input, var1, value1, var2, value2, var3, value3);
			}
		};
	}

}
