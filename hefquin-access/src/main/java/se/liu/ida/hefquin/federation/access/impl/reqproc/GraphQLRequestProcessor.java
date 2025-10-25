package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.GraphQLRequest;


public interface GraphQLRequestProcessor
	extends JSONRetrievalProcessor<GraphQLRequest,GraphQLEndpoint>
{
	@Override
	default boolean isSupportedRequestType( final Class<? extends DataRetrievalRequest> t ) {
		return GraphQLRequest.class.isAssignableFrom(t);
	}

	@Override
	default boolean isSupportedMemberType( final Class<? extends FederationMember> t ) {
		return GraphQLEndpoint.class.isAssignableFrom(t);
	}
}
