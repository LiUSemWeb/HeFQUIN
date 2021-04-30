package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;

public interface FederationAccessManager
{
	SolMapsResponse performRequest( SPARQLRequest req, SPARQLEndpoint fm );

	TPFResponse performRequest( TPFRequest req, TPFServer fm );

	TPFResponse performRequest( TPFRequest req, BRTPFServer fm );

	TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm );
}
