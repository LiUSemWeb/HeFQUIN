package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.Neo4jServer;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.Neo4jRequest;

public interface Neo4jRequestProcessor
	extends RecordsRetrievalProcessor<Neo4jRequest, Neo4jServer>
{
	@Override
	default boolean isSupportedRequestType( final Class<? extends DataRetrievalRequest> t ) {
		return Neo4jRequest.class.isAssignableFrom(t);
	}

	@Override
	default boolean isSupportedMemberType( final Class<? extends FederationMember> t ) {
		return Neo4jServer.class.isAssignableFrom(t);
	}
}
