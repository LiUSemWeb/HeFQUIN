package se.liu.ida.hefquin.federation.members.impl;

import java.util.Objects;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.federation.members.GraphQLEndpoint;

public class GraphQLEndpointImpl extends BaseForFederationMember
                                 implements GraphQLEndpoint
{
	protected final String url;
	protected final GraphQLSchema schema;
	protected final VocabularyMapping vm;

	public GraphQLEndpointImpl( final String url,
	                            final GraphQLSchema schema,
	                            final VocabularyMapping vm ) {
		assert url != null && ! url.isEmpty();
		assert schema != null;

		this.url = url;
		this.schema = schema;
		this.vm = vm;
	}

	@Override
	public VocabularyMapping getVocabularyMapping() { return vm; }
	@Override
	public String getURL() { return url; }

	@Override
	public GraphQLSchema getSchema() { return schema; }

	@Override
	public String toString() { return "GraphQL server at " + url; }

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		return    o instanceof GraphQLEndpoint ep
		       && ep.getURL().equals(url)
		       && ep.getSchema().equals(schema)
		       && Objects.equals( ep.getVocabularyMapping(), vm );
	}

}
