package se.liu.ida.hefquin.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;

public class GenericIntermediateResultBlockImpl<ElmtType>
                      implements IntermediateResultBlock<ElmtType>
{
	protected final List<ElmtType> elements = new ArrayList<ElmtType>();

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public Iterator<ElmtType> iterator() {
		return elements.iterator();
	}

	public void add( ElmtType element ) {
		elements.add(element);
	}
}
