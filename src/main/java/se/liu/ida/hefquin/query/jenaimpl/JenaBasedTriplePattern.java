package se.liu.ida.hefquin.query.jenaimpl;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.query.TriplePattern;

public class JenaBasedTriplePattern implements TriplePattern
{
	protected final Triple jenaObj;

	public JenaBasedTriplePattern( final Triple jenaObject ) {
		assert jenaObject != null;
		this.jenaObj = jenaObject;
	}

	public Triple asTriple() {
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

		if ( o != null && ! p.equals(s) && ! p.equals(p) )
			n++;

		return n;
	}

}
