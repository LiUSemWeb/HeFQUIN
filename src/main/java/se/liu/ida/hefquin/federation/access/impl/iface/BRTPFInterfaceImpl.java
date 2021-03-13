package se.liu.ida.hefquin.federation.access.impl.iface;

import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;

public class BRTPFInterfaceImpl extends TPFInterfaceImpl
{
	@Override
	public boolean supportsRequest( final DataRetrievalRequest req ) {
		return super.supportsBGPRequests() || req instanceof BindingsRestrictedTriplePatternRequest;
	}
}
