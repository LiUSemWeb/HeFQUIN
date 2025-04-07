package se.liu.ida.hefquin.engine.federation.access;

import java.util.Collection;
import java.util.Iterator;

import se.liu.ida.hefquin.base.data.Triple;

public interface TriplesResponse extends DataRetrievalResponse<Iterable<Triple>>
{
	/**
	 * Returns the number of triples that are returned by {@link #getResponseData()}.
	 */
	default int getSize() {
		try {
			final Iterable<Triple> triples = getResponseData();
			if ( triples instanceof Collection c ) {
				return c.size();
			}
			// Fallback to manual count
			int count = 0;
			for ( Iterator<Triple> it = triples.iterator(); it.hasNext(); it.next() ) {
				count++;
			}
			return count;
		} catch ( UnsupportedOperationDueToRetrievalError e ) {
			// intentionally do nothing
		}
		return -1;
	}
}
