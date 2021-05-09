package se.liu.ida.hefquin.engine.query;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.syntax.Element;

public interface SPARQLGraphPattern extends Query
{
	/**
	 * Returns a representation of this graph pattern as an
	 * object of the class {@link Element} of the Jena API.
	 *
	 * @deprecated use {@link #asJenaOp()} instead
	 */
	@Deprecated
	Element asJenaElement();

	/**
	 * Returns a representation of this graph pattern as an
	 * object of the interface {@link Op} of the Jena API.
	 */
	Op asJenaOp();
}
