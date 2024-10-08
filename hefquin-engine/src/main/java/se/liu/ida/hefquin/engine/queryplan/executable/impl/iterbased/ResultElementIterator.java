package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public interface ResultElementIterator extends Iterator<SolutionMapping>
{
	@Override
	boolean hasNext() throws ResultElementIterException;

	@Override
	SolutionMapping next() throws ResultElementIterException;
}
