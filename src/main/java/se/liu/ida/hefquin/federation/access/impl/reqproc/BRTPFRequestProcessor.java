package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public interface BRTPFRequestProcessor extends TPFRequestProcessor
{
	TriplesResponse performRequest( final BindingsRestrictedTriplePatternRequest req );
}
