package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

public interface SPARQLRequestProcessor
	extends SolMapRetrievalProcessor<SPARQLRequest,SPARQLEndpoint>
{

	@Override
	default boolean isSupportedRequestType( final Class<? extends DataRetrievalRequest> t ) {
		return SPARQLRequest.class.isAssignableFrom(t);
	}

	@Override
	default boolean isSupportedMemberType( final Class<? extends FederationMember> t ) {
		return SPARQLEndpoint.class.isAssignableFrom(t);
	}
}
