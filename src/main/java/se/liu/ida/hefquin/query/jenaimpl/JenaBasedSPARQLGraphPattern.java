package se.liu.ida.hefquin.query.jenaimpl;

import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.query.SPARQLGraphPattern;

public class JenaBasedSPARQLGraphPattern implements SPARQLGraphPattern
{
	protected final Element jenaPatternElement;

	public JenaBasedSPARQLGraphPattern( final Element jenaPatternElement ) {
		assert jenaPatternElement != null;
		this.jenaPatternElement = jenaPatternElement;
	}

	public Element asElement() {
		return jenaPatternElement;
	}

}
