package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class SolutionMappingsIterableWithSolMapFilter implements Iterable<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input;
	protected final SolutionMapping sm;

	public SolutionMappingsIterableWithSolMapFilter( final Iterable<SolutionMapping> input, final SolutionMapping sm ) {
		this.input = input;
		this.sm   = sm;
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return new SolutionMappingsIteratorWithSolMapFilter( input.iterator(), sm );
	}

}
