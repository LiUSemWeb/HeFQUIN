package se.liu.ida.hefquin.engine.query;

import org.apache.jena.graph.Triple;

public interface TriplePattern extends SPARQLGraphPattern
{
	/**
	 * Returns a representation of this triple pattern as an object of
	 * the class {@link org.apache.jena.graph.Triple} of the Jena API.
	 */
	Triple asJenaTriple();
	
	/**
	 * Returns the number of distinct variables in this triple pattern.
	 */
	int numberOfVars();
	
	/**
	 * Returns a string representation of the triple
	 */
	String toString();

}
