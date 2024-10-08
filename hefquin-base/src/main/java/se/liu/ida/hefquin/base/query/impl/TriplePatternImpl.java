package se.liu.ida.hefquin.base.query.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import se.liu.ida.hefquin.base.query.TriplePattern;

import java.util.Objects;

public class TriplePatternImpl implements TriplePattern
{
	protected final Triple jenaObj;

	public TriplePatternImpl( final Triple jenaObject ) {
		assert jenaObject != null;
		this.jenaObj = jenaObject;
	}

	public TriplePatternImpl( final Node s, final Node p, final Node o ) {
		this( Triple.create(s,p,o) );
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
		// wrapping the triple pattern into an ElementTriplesBlock
		// because the toString() function of that one uses pretty
		// printing via FormatterElement
		final ElementTriplesBlock e = new ElementTriplesBlock();
		e.addTriple( this.asJenaTriple() );
		return e.toString();
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
