package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.federation.access.CardinalityResponse;

public class CachedCardinalityResponse extends DataRetrievalResponseBase<Integer> implements CardinalityResponse
{
	/**
	 * Constructs a cached cardinality response.
	 *
	 * @param cardinality the cardinality of the request
	 */
	public CachedCardinalityResponse( final int cardinality ) {
		super( cardinality, null, null, new Date() );
	}
}