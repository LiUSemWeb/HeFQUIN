package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.federation.access.GraphQLInterface;

public interface GraphQLEndpoint extends FederationMember
{
	@Override
	GraphQLInterface getInterface();

	GraphQLSchema getSchema();
}
