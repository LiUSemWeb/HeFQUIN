package se.liu.ida.hefquin.federation.access;

import java.util.Collection;
import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public interface SolMapsResponse extends DataRetrievalResponse<Iterable<SolutionMapping>>
{
	/**
	 * Returns the number of solution mappings that are returned by
	 * {@link #getResponseData()}.
	 *
	 * @throws UnsupportedOperationDueToRetrievalError if the response to the
	 * corresponding request was an error message (see {@link #isError()}) or
	 * the response is defective (see {@link #isDefective()}).
	 */
	default int getSize() throws UnsupportedOperationDueToRetrievalError {
		final Iterable<SolutionMapping> mappings = getResponseData();
		if ( mappings instanceof Collection c ) {
			return c.size();
		}
		// Fallback to manual count
		int count = 0;
		for ( Iterator<SolutionMapping> it = mappings.iterator(); it.hasNext(); it.next() ) {
			count++;
		}
		return count;
	}
}
