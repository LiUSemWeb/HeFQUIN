package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

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
		return new SolutionMappingsIteratorWithOneVarFilter( input.iterator(), var, value );
	}

}
