package se.liu.ida.hefquin.federation.members.impl;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

public class SPARQLEndpointImpl extends BaseForRDFBasedFederationMember
                                implements SPARQLEndpoint
{
	protected final String url;

	public SPARQLEndpointImpl( final String url, final VocabularyMapping vm ) {
		super(vm);

		assert url != null && ! url.isEmpty();
		this.url = url;
	}

	@Override
	public String getURL() { return url; }

	@Override
	public String toString() { return "SPARQL endpoint at " + url; }

	@Override
	public boolean equals( final Object o ) {
		if ( super.equals(o) == false )
			return false;

		return    o instanceof SPARQLEndpoint ep
		       && ep.getURL().equals(url);
	}

}
