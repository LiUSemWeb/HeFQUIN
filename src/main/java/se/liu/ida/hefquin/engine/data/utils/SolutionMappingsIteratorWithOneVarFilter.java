package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterator of solution mappings that consumes another iterator
 * and passes on only the solution mappings that have a given value for a
 * given variable.
 */
public class SolutionMappingsIteratorWithOneVarFilter implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input;
	protected final Var var;
	protected final Node value;

	protected SolutionMapping nextOutputElement = null;

	public SolutionMappingsIteratorWithOneVarFilter( final Iterator<SolutionMapping> input, final Var var, final Node value ) {
		this.input = input;
		this.var   = var;
		this.value = value;
	}

	@Override
	public boolean hasNext() {
		while ( nextOutputElement == null && input.hasNext() ) {
			final SolutionMapping nextInputElement = input.next();
			final Node inputValue = nextInputElement.asJenaBinding().get(var);
			if ( inputValue == null || inputValue.equals(value) ) {
				nextOutputElement = nextInputElement;
			}
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
	};

}
