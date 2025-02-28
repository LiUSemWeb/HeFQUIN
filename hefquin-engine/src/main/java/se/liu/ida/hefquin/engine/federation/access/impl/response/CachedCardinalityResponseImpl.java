package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;

public class CachedCardinalityResponseImpl implements CardinalityResponse
{
	protected final FederationMember fm;
	protected final DataRetrievalRequest request;
	protected final int cardinality;
	protected final Date requestStartTime;
	protected final Date requestEndTime;

	public CachedCardinalityResponseImpl( final FederationMember fm,
	                                      final DataRetrievalRequest request,
	                                      final int cardinality,
	                                      final Date requestStartTime,
	                                      final Date requestEndTime ) {
		assert fm != null;
		assert request != null;

		this.fm = fm;
		this.request = request;
		this.cardinality = cardinality;
		this.requestStartTime = requestStartTime;
		this.requestEndTime = requestEndTime;
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
		return requestEndTime;
	}

	@Override
	public int getCardinality() {
		return cardinality;
	}

}
