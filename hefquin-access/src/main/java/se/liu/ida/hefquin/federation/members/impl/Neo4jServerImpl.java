package se.liu.ida.hefquin.federation.members.impl;

import se.liu.ida.hefquin.federation.members.Neo4jServer;

public class Neo4jServerImpl extends BaseForFederationMember
                             implements Neo4jServer
{
	protected final String url;

	public Neo4jServerImpl( final String url ) {
		assert url != null && ! url.isEmpty();

		this.url = url;
	}

	@Override
	public String getURL() { return url; }

	@Override
	public String toString() { return "Neo4j server at " + url; }

	@Override
	public boolean equals( final Object o ) {
		if ( super.equals(o) == false )
			return false;

		return    o instanceof Neo4jServer neo
		       && neo.getURL().equals(url);
	}

}
