package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.time.Duration;
import java.util.Date;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;

public class CardinalityResponseImpl implements CardinalityResponse
{
	protected final DataRetrievalResponse wrappedResponse;
	protected final DataRetrievalRequest request;
	protected final int cardinality;

    /**
     * Constructs a cardinality response that wraps the given data retrieval response and associates it
     * with a request and a cardinality value.
     *
     * @param wrappedResponse the wrapped data retrieval response (must not be null)
     * @param request         the original data retrieval request (must not be null)
     * @param cardinality     the cardinality of the request
     */
	public CardinalityResponseImpl( final DataRetrievalResponse wrappedResponse,
	                                final DataRetrievalRequest request,
	                                final int cardinality ) {
		assert wrappedResponse != null;
		assert request != null;

		this.wrappedResponse = wrappedResponse;
		this.request = request;
		this.cardinality = cardinality;
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
	public Duration getRequestDuration() {
		return wrappedResponse.getRequestDuration();
	}

	public int getCardinality() {
		return cardinality;
	}

	@Override
	public boolean isError() {
		return wrappedResponse.getErrorStatusCode() != null;
	};

	@Override
	public Integer getErrorStatusCode() {
		return wrappedResponse.getErrorStatusCode();
	};

	@Override
	public String getErrorDescription() {
		return wrappedResponse.getErrorDescription();
	};

}
