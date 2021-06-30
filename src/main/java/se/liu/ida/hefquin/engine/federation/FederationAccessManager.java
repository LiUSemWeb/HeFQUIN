package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;

public interface FederationAccessManager
{
	SolMapsResponse performRequest( SPARQLRequest req, SPARQLEndpoint fm );

	/**
	 * Requests the cardinality of the result of the given request.
	 *
	 * Assumes that the given request contains a {@link SPARQLGraphPattern}
	 * rather than a full {@link SPARQLQuery}. If it does not, then this
	 * method throws an {@link IllegalArgumentException}.
	 */
	CardinalityResponse performCardinalityRequest( SPARQLRequest req, SPARQLEndpoint fm );

	TPFResponse performRequest( TPFRequest req, TPFServer fm );

	TPFResponse performRequest( TPFRequest req, BRTPFServer fm );

	TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm );

	StringRetrievalResponse performRequest( Neo4jRequest req, Neo4jServer fm );
}
