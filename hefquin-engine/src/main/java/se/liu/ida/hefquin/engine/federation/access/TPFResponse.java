package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.data.Triple;

public interface TPFResponse extends TriplesResponse
{
	/**
	 * While {@link #getTriples()} returns an iterator over all triples
	 * contained in the given TPF response, this method here returns an
	 * iterator only over the matching triples that have been requested.
	 */
	Iterable<Triple> getPayload();

	/**
	 * Returns the number of triples that are returned by {@link #getPayload()}. 
	 */
	int getPayloadSize();

	/**
	 * Returns an iterator over all metadata triples contained in the
	 * given TPF response.
	 */
	Iterable<Triple> getMetadata();

	/**
	 * Returns the number of triples that are returned by {@link #getMetadata()}. 
	 */
	int getMetadataSize();

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
