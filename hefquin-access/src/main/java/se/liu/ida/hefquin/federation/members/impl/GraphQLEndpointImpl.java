package se.liu.ida.hefquin.federation.members.impl;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.federation.members.GraphQLEndpoint;

public class GraphQLEndpointImpl extends BaseForFederationMember
                                 implements GraphQLEndpoint
{
	protected final String url;
	protected final GraphQLSchema schema;

	public GraphQLEndpointImpl( final String url,
	                            final GraphQLSchema schema ) {
		assert url != null && ! url.isEmpty();
		assert schema != null;

		this.url = url;
		this.schema = schema;
	}

	@Override
	public String getURL() { return url; }

	@Override
	public GraphQLSchema getSchema() { return schema; }

	@Override
	public String toString() { return "GraphQL server at " + url; }

	@Override
	public boolean equals( final Object o ) {
		if ( super.equals(o) == false )
			return false;

		return    o instanceof GraphQLEndpoint ep
		       && ep.getURL().equals(url)
		       && ep.getSchema().equals(schema);
	}

}
