package se.liu.ida.hefquin.engine.query.impl;

import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;

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
	public Element asJenaElement() {
		return jenaPatternElement;
	}

	@Override
	public Op asJenaOp() {
		return Algebra.compile(jenaPatternElement);
	}

	@Override
	public String toString(){
		return asJenaElement().toString();
	}

}
