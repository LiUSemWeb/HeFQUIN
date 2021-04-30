package se.liu.ida.hefquin.federation.access;

import java.util.Iterator;

import se.liu.ida.hefquin.data.Triple;

public interface TriplesResponse extends DataRetrievalResponse
{
	Iterator<Triple> getIterator();

	/**
	 * Returns the number of triples that are returned by the {@link #getIterator()}. 
	 */
	int getSize();
}
