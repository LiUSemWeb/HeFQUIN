package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.federation.access.GraphQLInterface;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;

public interface GraphQLEndpoint extends FederationMember
{
	@Override
	GraphQLInterface getInterface();

	GraphQLSchema getSchema();
}
