package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;

public interface WrappedFederationMember extends FederationMember
{
	boolean isSupportedPattern( SPARQLGraphPattern p );
	boolean isSupportedNumberOfArguments( int n );
}
