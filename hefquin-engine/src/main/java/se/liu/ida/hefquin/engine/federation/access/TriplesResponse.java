package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.base.data.Triple;

public interface TriplesResponse extends DataRetrievalResponse
{
	Iterable<Triple> getTriples();

	/**
	 * Returns the number of triples that are returned by the {@link #getTriples()}. 
	 */
	int getSize();
}
