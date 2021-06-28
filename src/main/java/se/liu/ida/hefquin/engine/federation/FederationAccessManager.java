package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.StringRetrievalResponse;

public interface FederationAccessManager
{
	SolMapsResponse performRequest( SPARQLRequest req, SPARQLEndpoint fm );

	TPFResponse performRequest( TPFRequest req, TPFServer fm );

	TPFResponse performRequest( TPFRequest req, BRTPFServer fm );

	TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm );

	StringRetrievalResponse performRequest(Neo4jRequest req, Neo4jServer fm );
}
