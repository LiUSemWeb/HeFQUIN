package se.liu.ida.hefquin.engine.federation.access;

import java.util.Collection;
import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public interface SolMapsResponse extends DataRetrievalResponse<Iterable<SolutionMapping>>
{
	/**
	 * Returns the number of mappings that are returned by {@link #getSolutionMappings()}.
	 */
	default int getSize() {
		final Iterable<SolutionMapping> mappings = getResponseData();
		if ( mappings instanceof Collection ) {
			return ((Collection<SolutionMapping>) mappings).size();
		}
		// Fallback to manual count
		int count = 0;
		for ( Iterator<SolutionMapping> it = mappings.iterator(); it.hasNext(); it.next() ) {
			count++;
		}
		return count;
	}
}
