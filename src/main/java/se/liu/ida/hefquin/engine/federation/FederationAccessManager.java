package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;

public interface FederationAccessManager
{
	SolMapsResponse performRequest( SPARQLRequest req, SPARQLEndpoint fm );

	TPFResponse performRequest( TPFRequest req, TPFServer fm );

	TPFResponse performRequest( TPFRequest req, BRTPFServer fm );

	TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm );
}
