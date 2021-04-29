package se.liu.ida.hefquin.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;

public class GenericIntermediateResultBlockImpl
                      implements IntermediateResultBlock
{
	protected final List<SolutionMapping> elements = new ArrayList<>();

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return elements.iterator();
	}

	@Override
	public List<SolutionMapping> ArrayList() {
		return this.elements;
	}

	public void add( SolutionMapping element ) {
		elements.add(element);
	}
}
