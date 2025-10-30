package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.TPFServer;

public interface TPFRequestProcessor
	extends TriplesRetrievalProcessor<TPFRequest,TPFResponse,TPFServer>
{
	TPFResponse performRequest( TPFRequest req, BRTPFServer fm ) throws FederationAccessException;

	@Override
	default boolean isSupportedRequestType( final Class<? extends DataRetrievalRequest> t ) {
		return TPFRequest.class.isAssignableFrom(t);
	}

	@Override
	default boolean isSupportedResponseType( final Class<? extends DataRetrievalResponse<?>> t ) {
		return TPFResponse.class.isAssignableFrom(t);
	}

	@Override
	default boolean isSupportedMemberType( final Class<? extends FederationMember> t ) {
		return TPFServer.class.isAssignableFrom(t) || BRTPFServer.class.isAssignableFrom(t);
	}
}
