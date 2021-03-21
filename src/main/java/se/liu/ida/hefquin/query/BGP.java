package se.liu.ida.hefquin.query;

import java.util.Set;

public interface BGP extends SPARQLGraphPattern
{
	/**
	 * Returns an unmodifiable set of triple patterns.
	 */
	Set<? extends TriplePattern> getTriplePatterns(); 
}
