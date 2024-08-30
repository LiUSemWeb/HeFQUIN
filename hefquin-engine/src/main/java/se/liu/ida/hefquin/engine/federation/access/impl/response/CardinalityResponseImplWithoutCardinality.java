package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;

public class CardinalityResponseImplWithoutCardinality implements CardinalityResponse
{
	protected final DataRetrievalResponse wrappedResponse;
	protected final DataRetrievalRequest request;

	public CardinalityResponseImplWithoutCardinality( final DataRetrievalResponse wrappedResponse,
	                                                  final DataRetrievalRequest request ) {
		assert wrappedResponse != null;
		assert request != null;

		this.wrappedResponse = wrappedResponse;
		this.request = request;
	}

	public DataRetrievalResponse getWrappedResponse() {
		return wrappedResponse;
	}

	@Override
	public FederationMember getFederationMember() {
		return wrappedResponse.getFederationMember();
	}

	@Override
	public DataRetrievalRequest getRequest() {
		return request;
	}

	@Override
	public Date getRequestStartTime() {
		return wrappedResponse.getRequestStartTime();
	}

	@Override
	public Date getRetrievalEndTime() {
		return wrappedResponse.getRetrievalEndTime();
	}

	@Override
	public int getCardinality() {
		return Integer.MAX_VALUE;
	}

}
