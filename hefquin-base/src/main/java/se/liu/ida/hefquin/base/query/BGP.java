package se.liu.ida.hefquin.base.query;

import java.util.Set;

public interface BGP extends SPARQLGraphPattern
{
	/**
	 * Returns an unmodifiable set of triple patterns.
	 */
	Set<? extends TriplePattern> getTriplePatterns();


	/**
	 * Returns a string representation of the BGP
	 */
	String toString();

}
