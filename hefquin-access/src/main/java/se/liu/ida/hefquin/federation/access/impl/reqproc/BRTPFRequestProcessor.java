package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.TPFResponse;

public interface BRTPFRequestProcessor extends TriplesRetrievalProcessor<BRTPFRequest,BRTPFServer>
{
	@Override
	TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm ) throws FederationAccessException;
}
