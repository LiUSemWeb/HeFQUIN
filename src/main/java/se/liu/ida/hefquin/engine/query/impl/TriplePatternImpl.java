package se.liu.ida.hefquin.engine.query.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.TriplePattern;

public class TriplePatternImpl implements TriplePattern
{
	protected final Triple jenaObj;

	public TriplePatternImpl( final Triple jenaObject ) {
		assert jenaObject != null;
		this.jenaObj = jenaObject;
	}

	public TriplePatternImpl( final Node s, final Node p, final Node o ) {
		this( new Triple(s,p,o) );
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof TriplePattern && ((TriplePattern) o).asJenaTriple().equals(jenaObj);
	}

	@Override
	public Element asJenaElement() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Op asJenaOp() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Triple asJenaTriple() {
		return jenaObj;
	}

	@Override
	public int numberOfVars() {
		final String s = ( Var.isVar(jenaObj.getSubject()) ) ? jenaObj.getSubject().getName() : null;
		final String p = ( Var.isVar(jenaObj.getPredicate()) ) ? jenaObj.getPredicate().getName() : null;
		final String o = ( Var.isVar(jenaObj.getObject()) ) ? jenaObj.getObject().getName() : null;

		int n = 0;
		if ( s != null )
			n++;

		if ( p != null && ! p.equals(s) )
			n++;

		if ( o != null && ! o.equals(s) && ! o.equals(p) )
			n++;

		return n;
	}
	
	@Override
	public String toString() {
		return this.asJenaTriple().toString();
	}

}
