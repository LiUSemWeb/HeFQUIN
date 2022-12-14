package se.liu.ida.hefquin.engine.federation.access.utils;

import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;

public class SPARQLRequestUtils
{
	/**
	 * Merges the given triple pattern into the given request. If the given
	 * request is a {@link TriplePatternRequest} or a {@link BGPRequest}, then
	 * the resulting request is a {@link BGPRequest} with a BGP to which the
	 * triple pattern was added. Otherwise, the resulting request contains a
	 * {@link SPARQLGraphPattern} with the triple pattern joined to the pattern
	 * of the given request.
	 */
	public static SPARQLRequest merge( final SPARQLRequest req, final TriplePattern tp ) {
		final SPARQLGraphPattern mergedPattern = QueryPatternUtils.merge( tp, req.getQueryPattern() );
		if ( req instanceof BGP ) {
			return new BGPRequestImpl( (BGP) mergedPattern );
		}
		else {
			return new SPARQLRequestImpl(mergedPattern);
		}
	}

	/**
	 * Merges the given BGP into the given request. If the given request is a
	 * {@link TriplePatternRequest} or a {@link BGPRequest}, then the resulting
	 * request is a {@link BGPRequest} with a BGP to which the given BGP was
	 * added. Otherwise, the resulting request contains a {@link SPARQLGraphPattern}
	 * with the BGP joined to the pattern of the given request.
	 */
	public static SPARQLRequest merge( final SPARQLRequest req, final BGP bgp ) {
		final SPARQLGraphPattern mergedPattern = QueryPatternUtils.merge( bgp, req.getQueryPattern() );
		if ( req instanceof BGP ) {
			return new BGPRequestImpl( (BGP) mergedPattern );
		}
		else {
			return new SPARQLRequestImpl(mergedPattern);
		}
	}

}
