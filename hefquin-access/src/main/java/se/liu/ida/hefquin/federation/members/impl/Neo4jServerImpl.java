package se.liu.ida.hefquin.federation.members.impl;

import java.util.Objects;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.federation.members.Neo4jServer;

public class Neo4jServerImpl extends BaseForFederationMember
                             implements Neo4jServer
{
	protected final String url;
	protected final VocabularyMapping vm;

	public Neo4jServerImpl( final String url,
	                        final VocabularyMapping vm ) {
		assert url != null && ! url.isEmpty();

		this.url = url;
		this.vm = vm;
	}

	@Override
	public VocabularyMapping getVocabularyMapping() { return vm; }

	@Override
	public String getURL() { return url; }

	@Override
	public String toString() { return "Neo4j server at " + url; }

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		return    o instanceof Neo4jServer neo
		       && neo.getURL().equals(url)
		       && Objects.equals( neo.getVocabularyMapping(), vm );
	}

}
