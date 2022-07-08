package se.liu.ida.hefquin.engine.data.impl;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.engine.data.Triple;

public class TripleImpl implements Triple
{
	protected final org.apache.jena.graph.Triple jenaObj;

	public TripleImpl( final org.apache.jena.graph.Triple jenaObject ) {
		assert jenaObject != null;
		this.jenaObj = jenaObject;
	}

	public TripleImpl( final Node s, final Node p, final Node o ) {
		this( new org.apache.jena.graph.Triple(s,p,o) );
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof Triple && ((Triple) o).asJenaTriple().equals(jenaObj);
	}

	@Override
	public int hashCode(){
		return jenaObj.hashCode();
	}

	@Override
	public org.apache.jena.graph.Triple asJenaTriple() {
		return jenaObj;
	}

}
