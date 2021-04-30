package se.liu.ida.hefquin.federation.access;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;

public interface SolMapsResponse extends DataRetrievalResponse
{
	Iterator<SolutionMapping> getIterator();

	/**
	 * Returns the number of triples that are returned by the {@link #getIterator()}. 
	 */
	int getSize();
}
