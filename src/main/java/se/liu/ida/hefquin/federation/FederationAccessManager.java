package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public interface FederationAccessManager
{
	SolMapsResponse performRequest( SPARQLRequest req, SPARQLEndpoint fm );

	TriplesResponse performRequest( TriplePatternRequest req, TPFServer fm );

	TriplesResponse performRequest( BindingsRestrictedTriplePatternRequest req, BRTPFServer fm );
}
