package se.liu.ida.hefquin.engine.data.jenaimpl;

import se.liu.ida.hefquin.engine.data.Triple;

public class JenaBasedTriple implements Triple
{
	protected final org.apache.jena.graph.Triple jenaObj;

	public JenaBasedTriple( final org.apache.jena.graph.Triple jenaObject ) {
		assert jenaObject != null;
		this.jenaObj = jenaObject;
	}

	public org.apache.jena.graph.Triple asJenaTriple() {
		return jenaObj;
	}

}
