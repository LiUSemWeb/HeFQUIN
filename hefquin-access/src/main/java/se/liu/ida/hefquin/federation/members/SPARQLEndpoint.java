package se.liu.ida.hefquin.federation.members;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.federation.authentication.AuthenticationInformation;

public interface SPARQLEndpoint extends RDFBasedFederationMember
{
	/** Returns the URL at which this SPARQL endpoint can be reached. */
	String getURL();

	/** Returns the authentication information of this SPARQL endpoint. */
	AuthenticationInformation getAuthenticationInformation();

	@Override
	default boolean supportsMoreThanTriplePatterns() { return true; }

	@Override
	default boolean isSupportedPattern( final SPARQLGraphPattern p ) { return true; }
}
