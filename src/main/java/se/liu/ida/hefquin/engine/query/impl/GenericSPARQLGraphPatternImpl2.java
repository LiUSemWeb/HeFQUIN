package se.liu.ida.hefquin.engine.query.impl;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;

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

	@Override
	public Element asJenaElement() {
		return OpAsQuery.asQuery(jenaPatternOp).getQueryPattern();
	}

	@Override
	public Op asJenaOp() {
		return jenaPatternOp;
	}

	@Override
	public String toString(){
		return this.asJenaOp().toString();
	}

}

