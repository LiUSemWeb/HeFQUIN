package se.liu.ida.hefquin.federation.members.impl;

import java.util.Objects;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

public class SPARQLEndpointImpl extends BaseForFederationMember
                                implements SPARQLEndpoint
{
	protected final String url;
	protected final VocabularyMapping vm;

	public SPARQLEndpointImpl( final String url, final VocabularyMapping vm ) {
		assert url != null && ! url.isEmpty();

		this.url = url;
		this.vm = vm;
	}

	@Override
	public VocabularyMapping getVocabularyMapping() { return vm; }

	@Override
	public String getURL() { return url; }

	@Override
	public String toString() { return "SPARQL endpoint at " + url; }

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		return    o instanceof SPARQLEndpoint ep
		       && ep.getURL().equals(url)
		       && Objects.equals( ep.getVocabularyMapping(), vm );
	}

}
