package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;

public interface BRTPFRequestProcessor extends TriplesRetrievalProcessor<BRTPFRequest,BRTPFServer>
{
	@Override
	TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm ) throws FederationAccessException;
}
