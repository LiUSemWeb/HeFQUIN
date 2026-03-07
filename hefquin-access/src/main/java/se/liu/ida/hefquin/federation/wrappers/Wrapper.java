package se.liu.ida.hefquin.federation.wrappers;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;

/**
 * This interface captures any type of wrapper that allows HeFQUIN to interact
 * with some form of federation member that does not support RDF and SPARQL.
 */
public interface Wrapper
{
	boolean isSupportedPattern( SPARQLGraphPattern p );
	boolean isSupportedNumberOfArguments( int n );
}
