package se.liu.ida.hefquin.query.jenaimpl;

import org.apache.jena.graph.Node;

public class JenaBasedTriplePatternUtils
{
	public static JenaBasedTriplePattern createJenaBasedTriplePattern( final Node s, final Node p, final Node o ) {
		return new JenaBasedTriplePattern( new org.apache.jena.graph.Triple(s,p,o) );
	}

}
