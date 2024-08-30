package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public interface SolMapsResponse extends DataRetrievalResponse
{
	Iterable<SolutionMapping> getSolutionMappings();

	/**
	 * Returns the number of triples that are returned by {@link #getSolutionMappings()}. 
	 */
	int getSize();
}
