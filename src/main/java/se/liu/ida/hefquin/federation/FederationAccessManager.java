package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;

public interface FederationAccessManager
{
	SolMapsResponse performRequest( final SPARQLRequest req, final SPARQLEndpoint fm );

	// TODO: define this interface
}
