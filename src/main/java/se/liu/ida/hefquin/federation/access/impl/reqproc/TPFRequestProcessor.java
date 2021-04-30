package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;

public interface TPFRequestProcessor extends TriplesRetrievalProcessor<TPFRequest,TPFServer>
{
	@Override
	TPFResponse performRequest( TPFRequest req, TPFServer fm );

	TPFResponse performRequest( TPFRequest req, BRTPFServer fm );
}
