package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;

public abstract class DataRetrievalResponseBase implements DataRetrievalResponse
{
	private final FederationMember fm;
	private final DataRetrievalRequest request;
	private final Date requestStartTime;
	private final Date retrievalEndTime;

	/**
	 * Initializes the retrievalEndTime to the time when this object is created.
	 */
	protected DataRetrievalResponseBase( final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime ) {
		this( fm, request, requestStartTime, new Date() );
	}

	protected DataRetrievalResponseBase( final FederationMember fm,
	                                     final DataRetrievalRequest request,
	                                     final Date requestStartTime,
	                                     final Date retrievalEndTime ) {
		assert fm != null;
		assert request != null;
		assert requestStartTime != null;
		assert retrievalEndTime != null;

		this.fm = fm;
		this.request = request;
		this.requestStartTime = requestStartTime;
		this.retrievalEndTime = retrievalEndTime;
	}

	@Override
	public FederationMember getFederationMember() {
		return fm;
	}

	@Override
	public DataRetrievalRequest getRequest() {
		return request;
	}

	@Override
	public Date getRequestStartTime() {
		return requestStartTime;
	}

	@Override
	public Date getRetrievalEndTime() {
		return retrievalEndTime;
	}

}
