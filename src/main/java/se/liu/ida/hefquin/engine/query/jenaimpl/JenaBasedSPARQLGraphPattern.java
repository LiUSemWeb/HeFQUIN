package se.liu.ida.hefquin.engine.query.jenaimpl;

import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;

public class JenaBasedSPARQLGraphPattern implements SPARQLGraphPattern
{
	protected final Element jenaPatternElement;

	public JenaBasedSPARQLGraphPattern( final Element jenaPatternElement ) {
		assert jenaPatternElement != null;
		this.jenaPatternElement = jenaPatternElement;
	}

	@Override
	public Element asJenaElement() {
		return jenaPatternElement;
	}

}
