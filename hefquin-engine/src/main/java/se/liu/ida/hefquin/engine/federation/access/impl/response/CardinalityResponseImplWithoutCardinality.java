package se.liu.ida.hefquin.engine.federation.access.impl.response;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;

public class CardinalityResponseImplWithoutCardinality extends CardinalityResponseImpl
{
	public CardinalityResponseImplWithoutCardinality( final DataRetrievalResponse<?> wrappedResponse,
	                                                  final DataRetrievalRequest request ) {
		super( wrappedResponse, request, Integer.MAX_VALUE );
	}

	public Integer getErrorStatusCode() {
		return 500;
	};

}
