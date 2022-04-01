package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.federation.access.GraphQLInterface;

public interface GraphQLEndpoint extends FederationMember
{
	@Override
	GraphQLInterface getInterface();

}
