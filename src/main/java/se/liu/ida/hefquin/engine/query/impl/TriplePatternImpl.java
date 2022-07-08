package se.liu.ida.hefquin.engine.query.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.query.TriplePattern;

import java.util.Objects;

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
		return "(triple " + this.asJenaTriple().toString() + ") ";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TriplePattern)) return false;
		final TriplePattern that = (TriplePattern) o;
		return Objects.equals( jenaObj, that.asJenaTriple() );
	}

	@Override
	public int hashCode() {
		return jenaObj.hashCode();
	}
}
