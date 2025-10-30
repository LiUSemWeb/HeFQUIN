package se.liu.ida.hefquin.federation;

import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;

public interface Neo4jServer extends FederationMember
{
	/** Returns the URL at which this Neo4j server can be reached. */
	String getURL();

	@Override
	default boolean supportsMoreThanTriplePatterns() {
		return true;
	}

	@Override
	default boolean isSupportedPattern( final SPARQLGraphPattern p ) {
		if ( p instanceof TriplePattern )
			return true;

		if ( p instanceof BGP )
			return true;

		if (    p instanceof GenericSPARQLGraphPatternImpl1 gp
		     && gp.asJenaElement() instanceof ElementTriplesBlock )
			return true;

		if (    p instanceof GenericSPARQLGraphPatternImpl2 gp
		     && gp.asJenaOp() instanceof OpBGP )
			return true;

		return false;
	}
}
