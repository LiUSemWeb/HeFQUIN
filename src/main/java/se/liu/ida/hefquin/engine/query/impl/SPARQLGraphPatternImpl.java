package se.liu.ida.hefquin.engine.query.impl;

import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;

public class SPARQLGraphPatternImpl implements SPARQLGraphPattern
{
	protected final Element jenaPatternElement;
	protected final Op jenaPatternOp;

	public SPARQLGraphPatternImpl( final Element jenaPatternElement ) {
		assert jenaPatternElement != null;
		this.jenaPatternElement = jenaPatternElement;

		jenaPatternOp = Algebra.compile(jenaPatternElement);
	}

	public SPARQLGraphPatternImpl( final Op jenaPatternOp ) {
		assert jenaPatternOp != null;
		this.jenaPatternOp = jenaPatternOp;

		jenaPatternElement = OpAsQuery.asQuery(jenaPatternOp).getQueryPattern();
	}

	@Override
	public Element asJenaElement() {
		return jenaPatternElement;
	}

	@Override
	public Op asJenaOp() {
		return jenaPatternOp;
	}

}
