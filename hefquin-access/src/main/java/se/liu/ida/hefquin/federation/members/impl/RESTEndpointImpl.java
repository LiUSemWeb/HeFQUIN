package se.liu.ida.hefquin.federation.members.impl;

import java.util.List;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;

public class RESTEndpointImpl extends BaseForFederationMember
                              implements RESTEndpoint
{
	protected final String urlTemplate;
	protected final List<RESTEndpoint.Parameter> params;

	public RESTEndpointImpl( final String urlTemplate, final List<RESTEndpoint.Parameter> params ) {
		assert urlTemplate != null && ! urlTemplate.isEmpty();
		this.urlTemplate = urlTemplate;
		this.params = (params == null) ? List.of() : params;
	}

	@Override
	public String getURLTemplate() { return urlTemplate; }

	@Override
	public int getNumberOfParameters() { return params.size(); }

	@Override
	public RESTEndpoint.Parameter getParameterByName(String name) {
		for (RESTEndpoint.Parameter param : params) {
			if (param.getName().equals(name)) {
				return param;
			}
		}
		return null;
	}

	@Override
	public Iterable<Parameter> getParameters() { return params; }

	@Override
	public boolean supportsMoreThanTriplePatterns() { return true; }

	@Override
	public boolean isSupportedPattern( final SPARQLGraphPattern p ) { return true; }

	@Override
	public String toString() { return "REST endpoint at " + urlTemplate; }

	@Override
	public boolean equals( final Object o ) {
		if ( super.equals(o) == false )
			return false;

		return    o instanceof RESTEndpoint ep
		       && ep.getURLTemplate().equals(urlTemplate)
		       && ep.getParameters().equals(params);
	}

}
