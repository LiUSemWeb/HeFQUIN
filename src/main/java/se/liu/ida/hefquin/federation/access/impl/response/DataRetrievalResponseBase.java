package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;

public abstract class DataRetrievalResponseBase implements DataRetrievalResponse
{
	private final FederationMember fm;
	private final Date retrievalTime;

	/**
	 * Initializes the retrievalTime to the time when this object is created.
	 */
	protected DataRetrievalResponseBase( final FederationMember fm ) {
		this( fm, new Date() );
	}

	protected DataRetrievalResponseBase( final FederationMember fm,
	                                     final Date retrievalTime ) {
		assert fm != null;
		assert retrievalTime != null;

		this.fm = fm;
		this.retrievalTime = retrievalTime;
	}
	
	public FederationMember getFederationMember() {
		return fm;
	}

	public Date getRetrievalTime() {
		return retrievalTime;
	}

}
