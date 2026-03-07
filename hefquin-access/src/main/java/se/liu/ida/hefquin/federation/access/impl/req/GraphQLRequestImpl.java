package se.liu.ida.hefquin.federation.access.impl.req;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;
import se.liu.ida.hefquin.federation.access.GraphQLRequest;

public class GraphQLRequestImpl implements GraphQLRequest
{
	protected final GraphQLQuery query;

	public GraphQLRequestImpl( final GraphQLQuery query ) {
		assert query != null;
		this.query = query;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return null;
	}

	@Override
	public GraphQLQuery getGraphQLQuery() {
		return query;
	}

}
