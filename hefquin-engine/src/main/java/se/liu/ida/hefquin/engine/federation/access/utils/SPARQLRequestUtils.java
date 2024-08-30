package se.liu.ida.hefquin.engine.federation.access.utils;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;

public class SPARQLRequestUtils
{
	/**
	 * Merges the given graph pattern into the given request. Returns a
	 * {@link BGPRequest} if possible (namely, if the given request is a
	 * {@link TriplePatternRequest} or a {@link BGPRequest} and the given
	 * pattern is a {@link BGP} or a {@link TriplePattern}).
	 */
	public static SPARQLRequest merge( final SPARQLRequest req, final SPARQLGraphPattern pattern ) {
		final SPARQLGraphPattern mergedPattern = QueryPatternUtils.merge( pattern, req.getQueryPattern() );
		if ( req instanceof BGP ) {
			return new BGPRequestImpl( (BGP) mergedPattern );
		}
		else {
			return new SPARQLRequestImpl(mergedPattern);
		}
	}

}