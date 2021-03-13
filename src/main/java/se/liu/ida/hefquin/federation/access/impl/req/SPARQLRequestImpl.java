package se.liu.ida.hefquin.federation.access.impl.req;

import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.query.SPARQLGraphPattern;

public class SPARQLRequestImpl implements SPARQLRequest
{
	protected final SPARQLGraphPattern pattern;

	public SPARQLRequestImpl( final SPARQLGraphPattern pattern ) {
		assert pattern != null;
		this.pattern = pattern;
	}

	public SPARQLGraphPattern getQueryPattern() {
		return pattern;
	}

}
