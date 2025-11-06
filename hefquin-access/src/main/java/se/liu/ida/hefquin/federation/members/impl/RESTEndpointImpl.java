package se.liu.ida.hefquin.federation.members.impl;

import java.util.List;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;

public class RESTEndpointImpl extends BaseForFederationMember
                              implements RESTEndpoint
{
	protected final String url;
	protected final List<RESTEndpoint.Parameter> params;

	public RESTEndpointImpl( final String url, final List<RESTEndpoint.Parameter> params ) {
		assert url != null && ! url.isEmpty();

		this.url = url;
		this.params = (params == null) ? List.of() : params;
	}

	@Override
	public String getURL() { return url; }

	@Override
	public Iterable<Parameter> getParameters() { return params; }

	@Override
	public boolean supportsMoreThanTriplePatterns() { return true; }

	@Override
	public boolean isSupportedPattern( final SPARQLGraphPattern p ) { return true; }

	@Override
	public String toString() { return "REST endpoint at " + url; }

	@Override
	public boolean equals( final Object o ) {
		if ( super.equals(o) == false )
			return false;

		return    o instanceof RESTEndpoint ep
		       && ep.getURL().equals(url)
		       && ep.getParameters().equals(params);
	}

}
