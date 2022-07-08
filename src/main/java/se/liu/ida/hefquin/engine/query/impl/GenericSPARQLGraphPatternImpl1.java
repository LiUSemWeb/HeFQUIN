package se.liu.ida.hefquin.engine.query.impl;

import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;

/**
 * This class is a generic implementation of {@link SPARQLGraphPattern}
 * in which this graph pattern is given as an object of the class
 * {@link Element} of the Jena API.
 */
public class GenericSPARQLGraphPatternImpl1 implements SPARQLGraphPattern
{
	protected final Element jenaPatternElement;

	public GenericSPARQLGraphPatternImpl1( final Element jenaPatternElement ) {
		assert jenaPatternElement != null;
		this.jenaPatternElement = jenaPatternElement;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o instanceof GenericSPARQLGraphPatternImpl1 ) {
			final GenericSPARQLGraphPatternImpl1 oo = (GenericSPARQLGraphPatternImpl1) o;
			if ( oo.jenaPatternElement.equals(jenaPatternElement) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return jenaPatternElement.hashCode();
	}

	public Element asJenaElement() {
		return jenaPatternElement;
	}

	/**
	 * Avoid using this function because, when called, it compiles the
	 * internal {@link Element} object into an {@link Op} object.
	 */
	@Deprecated
	public Op asJenaOp() {
		return Algebra.compile(jenaPatternElement);
	}

	@Override
	public String toString(){
		return asJenaElement().toString();
	}

}
