package se.liu.ida.hefquin.engine.query.impl;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;

/**
 * This class is a generic implementation of {@link SPARQLGraphPattern}
 * in which this graph pattern is given as an object of the class
 * {@link Op} of the Jena API.
 */
public class GenericSPARQLGraphPatternImpl2 implements SPARQLGraphPattern
{
	protected final Op jenaPatternOp;

	public GenericSPARQLGraphPatternImpl2( final Op jenaPatternOp ) {
		assert jenaPatternOp != null;
		this.jenaPatternOp = jenaPatternOp;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o instanceof GenericSPARQLGraphPatternImpl2 ) {
			final GenericSPARQLGraphPatternImpl2 oo = (GenericSPARQLGraphPatternImpl2) o;
			if ( oo.jenaPatternOp.equals(jenaPatternOp) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Avoid using this function. It converts the internal {@link Op} object
	 * back into an {@link Element}, which might not work correctly in all
	 * cases?
	 */
	@Deprecated
	public Element asJenaElement() {
		return OpAsQuery.asQuery(jenaPatternOp).getQueryPattern();
	}

	public Op asJenaOp() {
		return jenaPatternOp;
	}

	@Override
	public String toString(){
		return this.asJenaOp().toString();
	}

}

