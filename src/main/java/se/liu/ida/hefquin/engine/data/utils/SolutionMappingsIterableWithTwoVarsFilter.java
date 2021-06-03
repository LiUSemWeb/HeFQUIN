package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class SolutionMappingsIterableWithTwoVarsFilter implements Iterable<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input;
	protected final Var var1, var2;
	protected final Node value1, value2;

	public SolutionMappingsIterableWithTwoVarsFilter(
			final Iterable<SolutionMapping> input,
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
	{
		this.input = input;
		this.var1   = var1;
		this.var2   = var2;
		this.value1 = value1;
		this.value2 = value2;
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return new SolutionMappingsIteratorWithTwoVarsFilter( input.iterator(), var1, value1, var2, value2 );
	}

}
