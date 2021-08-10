package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;

public interface FederationAccessManager
{
	void issueRequest( SPARQLRequest req, SPARQLEndpoint fm, ResponseProcessor<SolMapsResponse> respProc ) throws FederationAccessException;

	/**
	 * Requests the cardinality of the result of the given request.
	 *
	 * Assumes that the given request contains a {@link SPARQLGraphPattern}
	 * rather than a full {@link SPARQLQuery}. If it does not, then this
	 * method throws an {@link IllegalArgumentException}.
	 */
	void issueCardinalityRequest( SPARQLRequest req, SPARQLEndpoint fm, ResponseProcessor<CardinalityResponse> respProc ) throws FederationAccessException;

	void issueRequest( TPFRequest req, TPFServer fm, ResponseProcessor<TPFResponse> respProc ) throws FederationAccessException;

	void issueRequest( TPFRequest req, BRTPFServer fm, ResponseProcessor<TPFResponse> respProc ) throws FederationAccessException;

	void issueRequest( BRTPFRequest req, BRTPFServer fm, ResponseProcessor<TPFResponse> respProc ) throws FederationAccessException;

	void issueRequest( Neo4jRequest req, Neo4jServer fm, ResponseProcessor<StringResponse> respProc ) throws FederationAccessException;
}
