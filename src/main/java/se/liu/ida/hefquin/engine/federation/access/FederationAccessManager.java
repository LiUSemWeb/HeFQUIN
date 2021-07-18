package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;

public interface FederationAccessManager
{
	SolMapsResponse performRequest( SPARQLRequest req, SPARQLEndpoint fm ) throws FederationAccessException;

	/**
	 * Requests the cardinality of the result of the given request.
	 *
	 * Assumes that the given request contains a {@link SPARQLGraphPattern}
	 * rather than a full {@link SPARQLQuery}. If it does not, then this
	 * method throws an {@link IllegalArgumentException}.
	 */
	CardinalityResponse performCardinalityRequest( SPARQLRequest req, SPARQLEndpoint fm ) throws FederationAccessException;

	TPFResponse performRequest( TPFRequest req, TPFServer fm ) throws FederationAccessException;

	TPFResponse performRequest( TPFRequest req, BRTPFServer fm ) throws FederationAccessException;

	TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm ) throws FederationAccessException;

	StringResponse performRequest( Neo4jRequest req, Neo4jServer fm ) throws FederationAccessException;
}
