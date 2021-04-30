package se.liu.ida.hefquin.federation.access;

import java.util.Iterator;

import se.liu.ida.hefquin.data.Triple;

public interface TPFResponse extends TriplesResponse
{
	/**
	 * While {@link #getIterator()} returns an iterator over all triples
	 * contained in the given TPF response, this method here returns an
	 * iterator only over the matching triples that have been requested.
	 */
	Iterator<Triple> getPayloadIterator();

	/**
	 * Returns the number of triples that are returned by the {@link #getPayloadIterator()}. 
	 */
	int getPayloadSize();

	/**
	 * Returns an iterator over all metadata triples contained in the
	 * given TPF response.
	 */
	Iterator<Triple> getMetadataIterator();

	/**
	 * Returns the number of triples that are returned by the {@link #getMetadataIterator()}. 
	 */
	int getMetadataSize();

	/**
	 * Returns true if the metadata of the given TPF response indicates
	 * that this response is the last page of matching triples.
	 *
	 * Returns null of there is no metadata related to paging. 
	 */
	Boolean isLastPage();

	/**
	 * Returns the cardinality estimate provided by as metadata in the
	 * given TPF response. Returns null if there is no metadata with a
	 * cardinality estimate.
	 */
	Integer getCardinalityEstimate();
}
