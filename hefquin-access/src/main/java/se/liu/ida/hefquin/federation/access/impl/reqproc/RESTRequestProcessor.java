package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.RESTRequest;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.impl.RequestProcessor;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;

public interface RESTRequestProcessor extends RequestProcessor<RESTRequest,
                                                               StringResponse,
                                                               RESTEndpoint>
{
	@Override
	default boolean isSupportedRequestType( final Class<? extends DataRetrievalRequest> t ) {
		return RESTRequest.class.isAssignableFrom(t);
	}

	@Override
	default boolean isSupportedMemberType( final Class<? extends FederationMember> t ) {
		return RESTEndpoint.class.isAssignableFrom(t);
	}

	@Override
	default boolean isSupportedResponseType( final Class<? extends DataRetrievalResponse<?>> t ) {
		return StringResponse.class.isAssignableFrom(t);
	}
}
