package se.liu.ida.hefquin.engine.query;

import org.apache.jena.sparql.algebra.Op;

public interface SPARQLGraphPattern extends Query
{
	/**
	 * Returns a representation of this graph pattern as an
	 * object of the interface {@link Op} of the Jena API.
	 */
	Op asJenaOp();
}
