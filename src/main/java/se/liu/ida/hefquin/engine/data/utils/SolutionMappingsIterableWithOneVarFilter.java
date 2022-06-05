package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterable of solution mappings that wraps another such iterable
 * and that can be used to iterate over the subset of solution mappings that
 * have a given value for a given variable.
 */
public class SolutionMappingsIterableWithOneVarFilter implements Iterable<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input;
	protected final Var var;
	protected final Node value;

	public SolutionMappingsIterableWithOneVarFilter( final Iterable<SolutionMapping> input, final Var var, final Node value ) {
		this.input = input;
		this.var   = var;
		this.value = value;
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return new FilteringIteratorForSolMaps_OneVarBinding( input.iterator(), var, value );
	}

}
