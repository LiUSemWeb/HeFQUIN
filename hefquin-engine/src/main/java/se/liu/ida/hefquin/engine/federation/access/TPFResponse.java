package se.liu.ida.hefquin.engine.federation.access;

import java.util.Collection;
import java.util.Iterator;

import se.liu.ida.hefquin.base.data.Triple;

public interface TPFResponse extends TriplesResponse
{
	/**
	 * Returns the number of triples contained in this response,
	 * which considers both the payload and the metadata.
	 */
	default int getSize() throws UnsupportedOperationDueToRetrievalError {
		return getPayloadSize() + getMetadataSize();
	}

	/**
	 * While {@link #getResponseData()} returns an iterator over all triples contained in the given TPF response,this method
	 * here returns an iterator only over the matching triples that have been requested.
	 *
	 * @throws UnsupportedOperationDueToRetrievalError
	 */
	Iterable<Triple> getPayload() throws UnsupportedOperationDueToRetrievalError;

	/**
	 * Returns the number of triples that are returned by {@link #getPayload()}.
	 *
	 * @throws UnsupportedOperationDueToRetrievalError
	 */
	default int getPayloadSize() throws UnsupportedOperationDueToRetrievalError {
		final Iterable<Triple> triples = getPayload();
		if ( triples instanceof Collection c ) {
			return c.size();
		}
		// Fallback to manual count
		int count = 0;
		for ( Iterator<Triple> it = triples.iterator(); it.hasNext(); it.next() ) {
			count++;
		}
		return count;
	}

	/**
	 * Returns an iterator over all metadata triples contained in the given TPF response.
	 */
	Iterable<Triple> getMetadata();

	/**
	 * Returns the number of triples that are returned by {@link #getMetadata()}. 
	 */
	default int getMetadataSize(){
		final Iterable<Triple> triples = getMetadata();
		if ( triples instanceof Collection c ) {
			return c.size();
		}
		// Fallback to manual count
		int count = 0;
		for ( Iterator<Triple> it = triples.iterator(); it.hasNext(); it.next() ) {
			count++;
		}
		return count;
	}

	/**
	 * Returns <code>true</code> if the metadata of the given TPF response
	 * indicates that this response is the last page of matching triples.
	 *
	 * Returns <code>null</code> of there is no metadata related to paging.
	 */
	Boolean isLastPage();

	/**
	 * Returns the URL via which the next page of the TPF can be requested.
	 * Returns <code>null</code> of no such URL is mentioned in the metadata.
	 */
	String getNextPageURL();

	/**
	 * Returns the cardinality estimate provided as metadata in the given
	 * TPF response. Returns <code>null</code> if there is no metadata
	 * with a cardinality estimate.
	 */
	Integer getCardinalityEstimate();
}
