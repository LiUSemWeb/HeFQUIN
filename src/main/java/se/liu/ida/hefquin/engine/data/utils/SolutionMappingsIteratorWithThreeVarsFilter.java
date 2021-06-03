package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterator of solution mappings that consumes another iterator
 * and passes on only the solution mappings that have given values for three
 * variables.
 */
public class SolutionMappingsIteratorWithThreeVarsFilter implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input;
	protected final Var var1, var2, var3;
	protected final Node value1, value2, value3;

	protected SolutionMapping nextOutputElement = null;

	public SolutionMappingsIteratorWithThreeVarsFilter(
			final Iterator<SolutionMapping> input,
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
	{
		this.input = input;
		this.var1   = var1;
		this.var2   = var2;
		this.var3   = var3;
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
	}

	@Override
	public boolean hasNext() {
		while ( nextOutputElement == null && input.hasNext() ) {
			final SolutionMapping nextInputElement = input.next();

			final Binding b = nextInputElement.asJenaBinding();
			final Node inputValue1 = b.get(var1);
			final Node inputValue2 = b.get(var2);
			final Node inputValue3 = b.get(var3);

			if ( inputValue1 == null || inputValue1.equals(value1) ) {
				if ( inputValue2 == null || inputValue2.equals(value2) ) {
					if ( inputValue3 == null || inputValue3.equals(value3) ) {
						nextOutputElement = nextInputElement;
					}
				}
			}
		}

		return ( nextOutputElement != null );
	}

	@Override
	public SolutionMapping next() {
		if ( ! hasNext() )
			throw new NoSuchElementException();

		return nextOutputElement;
	};

}
