package se.liu.ida.hefquin.engine.queryplan.executable;

import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public interface IntermediateResultElementSink
{
	void send( final SolutionMapping element );

	/**
	 * Consumes the given iterator, calling {@link #send(SolutionMapping)}
	 * for every {@link SolutionMapping} obtained from the iterator, and
	 * returns the number of solution mappings obtained from the iterator.
	 */
	default int send( final Iterable<SolutionMapping> it ) {
		return send( it.iterator() );
	}

	/**
	 * Consumes the given iterator, calling {@link #send(SolutionMapping)}
	 * for every {@link SolutionMapping} obtained from the iterator, and
	 * returns the number of solution mappings obtained from the iterator.
	 */
	default int send( final Iterator<SolutionMapping> it ) {
		int cnt = 0;
		while( it.hasNext() ) {
			cnt++;
			send( it.next() );
		}
		return cnt;
	}

}
