package se.liu.ida.hefquin.query;

public interface TriplePattern extends SPARQLGraphPattern
{
	/**
	 * Returns the number of distinct variables in this triple pattern.
	 */
	int numberOfVars();

}
