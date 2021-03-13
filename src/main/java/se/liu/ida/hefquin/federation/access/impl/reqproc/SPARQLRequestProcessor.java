package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;

public interface SPARQLRequestProcessor extends SolMapRetrievalProcessor
{
	SolMapsResponse performRequest( final SPARQLRequest req );
}
