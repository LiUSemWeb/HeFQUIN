package se.liu.ida.hefquin.engine.federation.access;

import java.util.Collection;
import java.util.Iterator;

import se.liu.ida.hefquin.base.data.Triple;

public interface TriplesResponse extends DataRetrievalResponse<Iterable<Triple>>
{
	Iterable<Triple> getTriples();

	/**
	 * Returns the number of triples that are returned by the {@link #getTriples()}. 
	 */
	default int getSize(){
		final Iterable<Triple> triples = getTriples();
		if (triples instanceof Collection) {
			return ((Collection<Triple>) triples).size();
		}
		// Fallback to manual count
		int count = 0;
		for ( Iterator<Triple> it = triples.iterator(); it.hasNext(); it.next() ){
			count++;
		}
		return count;
	}
}
