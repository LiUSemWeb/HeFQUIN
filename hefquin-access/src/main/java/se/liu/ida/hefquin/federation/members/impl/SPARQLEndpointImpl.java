package se.liu.ida.hefquin.federation.members.impl;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.federation.authentication.AuthenticationInformation;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

public class SPARQLEndpointImpl extends BaseForRDFBasedFederationMember
                                implements SPARQLEndpoint
{
	protected final String url;
	protected final AuthenticationInformation authInfo;

	public SPARQLEndpointImpl( final String url, final AuthenticationInformation authInfo, final VocabularyMapping vm ) {
		super(vm);

		assert url != null && ! url.isEmpty();
		this.url = url;

		this.authInfo = authInfo;
	}

	@Override
	public String getURL() { return url; }

	@Override
	public AuthenticationInformation getAuthenticationInformation() { return authInfo; }

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
