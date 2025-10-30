package se.liu.ida.hefquin.federation;

import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.federation.access.TPFRequest;

public interface TPFServer extends FederationMember
{
	String getBaseURL();

	String createRequestURL( TPFRequest req );

	@Override
	default boolean supportsMoreThanTriplePatterns() {
		return false;
	}

	@Override
	default boolean isSupportedPattern( final SPARQLGraphPattern p ) {
		if ( p instanceof TriplePattern )
			return true;

		if (    p instanceof GenericSPARQLGraphPatternImpl1 gp
		     && gp.asJenaElement() instanceof ElementTriplesBlock e
		     && e.getPattern().size() == 1 )
			return true;

		if (    p instanceof GenericSPARQLGraphPatternImpl2 gp
		     && gp.asJenaOp() instanceof OpBGP opBGP
		     && opBGP.getPattern().size() == 1 )
			return true;

		return false;
	}
}
