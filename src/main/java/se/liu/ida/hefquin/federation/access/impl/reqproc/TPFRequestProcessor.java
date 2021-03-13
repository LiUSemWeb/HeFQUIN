package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public interface TPFRequestProcessor extends TriplesRetrievalProcessor
{
	TriplesResponse performRequest( final TriplePatternRequest req );
}
