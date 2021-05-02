package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;

public class GenericIntermediateResultBlockImpl
                      implements IntermediateResultBlock
{
	protected final List<SolutionMapping> elements = new ArrayList<SolutionMapping>();

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public Iterable<SolutionMapping> getSolutionMappings() {
		return elements;
	}

	public void add( SolutionMapping element ) {
		elements.add(element);
	}
}
