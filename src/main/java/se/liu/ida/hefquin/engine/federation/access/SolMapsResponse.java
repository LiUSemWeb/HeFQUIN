package se.liu.ida.hefquin.engine.federation.access;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public interface SolMapsResponse extends DataRetrievalResponse
{
	Iterator<SolutionMapping> getIterator();

	/**
	 * Returns the number of triples that are returned by the {@link #getIterator()}. 
	 */
	int getSize();
}
