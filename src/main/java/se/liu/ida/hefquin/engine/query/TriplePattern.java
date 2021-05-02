package se.liu.ida.hefquin.engine.query;

public interface TriplePattern extends SPARQLGraphPattern
{
	/**
	 * Returns the number of distinct variables in this triple pattern.
	 */
	int numberOfVars();

}
