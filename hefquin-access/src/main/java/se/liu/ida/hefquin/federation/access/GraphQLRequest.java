package se.liu.ida.hefquin.federation.access;

import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;

public interface GraphQLRequest extends DataRetrievalRequest
{
	/**
	 * Returns the GraphQL query to be issued by this request.
	 */
	GraphQLQuery getGraphQLQuery();
}
