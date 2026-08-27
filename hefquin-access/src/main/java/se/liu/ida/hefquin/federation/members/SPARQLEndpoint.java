package se.liu.ida.hefquin.federation.members;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;

public interface SPARQLEndpoint extends RDFBasedFederationMember
{
	/** Returns the URL at which this SPARQL endpoint can be reached. */
	String getURL();

	@Override
	default boolean supportsMoreThanTriplePatterns() { return true; }

	@Override
	default boolean isSupportedPattern( final SPARQLGraphPattern p ) { return true; }
}
