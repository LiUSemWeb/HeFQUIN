package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterable of solution mappings that wraps another such iterable
 * and that can be used to iterate over the subset of solution mappings that
 * have given values for three variables.
 */
public class SolutionMappingsIterableWithThreeVarsFilter implements Iterable<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input;
	protected final Var var1, var2, var3;
	protected final Node value1, value2, value3;

	public SolutionMappingsIterableWithThreeVarsFilter(
			final Iterable<SolutionMapping> input,
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
	public Iterator<SolutionMapping> iterator() {
		return new SolutionMappingsIteratorWithThreeVarsFilter( input.iterator(), var1, value1, var2, value2, var3, value3 );
	}

}
