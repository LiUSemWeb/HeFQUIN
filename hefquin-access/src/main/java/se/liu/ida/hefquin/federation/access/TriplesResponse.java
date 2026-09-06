package se.liu.ida.hefquin.federation.access;

import java.util.Collection;
import java.util.Iterator;

import se.liu.ida.hefquin.base.data.Triple;

public interface TriplesResponse extends DataRetrievalResponse<Iterable<Triple>>
{
	/**
	 * Returns the number of triples contained in this response, or throws
	 * an exception ({@link UnsupportedOperationDueToRetrievalError}) if
	 * either the response to the corresponding request was an error message
	 * (see {@link #isError()}) or the response is defective (see
	 * {@link #isDefective()}).
	 */
	default int getSize() throws UnsupportedOperationDueToRetrievalError {
		final Iterable<Triple> triples = getResponseData();
		if ( triples instanceof Collection c ) {
			return c.size();
		}

		// Fallback to manual count
		int count = 0;
		final Iterator<Triple> it = triples.iterator();
		while ( it.hasNext() ) { it.next(); count++; }
		return count;
	}
}
