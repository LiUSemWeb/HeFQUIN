package se.liu.ida.hefquin.federation.access.utils;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class SPARQLRequestUtils
{
	/**
	 * Merges the given graph pattern into the given request. Returns a
	 * {@link BGPRequest} if possible (namely, if the given request is a
	 * {@link TriplePatternRequest} or a {@link BGPRequest} and the given
	 * pattern is a {@link BGP} or a {@link TriplePattern}).
	 */
	public static SPARQLRequest merge( final SPARQLRequest req, final SPARQLGraphPattern pattern ) {
		final SPARQLGraphPattern mergedPattern = pattern.mergeWith( req.getQueryPattern() );
		if ( req instanceof BGP bgp ) {
			return new BGPRequestImpl(bgp);
		}
		else {
			return new SPARQLRequestImpl(mergedPattern);
		}
	}

}
