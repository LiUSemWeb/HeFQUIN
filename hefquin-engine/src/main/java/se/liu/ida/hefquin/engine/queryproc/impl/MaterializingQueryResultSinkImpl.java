package se.liu.ida.hefquin.engine.queryproc.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryproc.QueryResultSink;

/**
 * An implementation of {@link QueryResultSink}
 * that simply materializes the query result.
 */
public class MaterializingQueryResultSinkImpl implements QueryResultSink
{
	protected List<SolutionMapping> l = new ArrayList<>();

	@Override
	public void send( final SolutionMapping element ) {
		l.add(element);
	}

	/**
	 * Returns an iterator over the materialized result.
	 */
	public Iterator<SolutionMapping> getSolMapsIter() {
		return l.iterator();
	}

	/**
	 * Returns the size of the materialized result. 
	 */
	public int size() {
		return l.size();
	}
}
