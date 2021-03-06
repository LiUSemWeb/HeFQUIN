package se.liu.ida.hefquin.engine.federation.access.impl.iface;

import se.liu.ida.hefquin.engine.federation.access.BRTPFInterface;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;

public class BRTPFInterfaceImpl extends TPFInterfaceImpl implements BRTPFInterface
{
	@Override
	public boolean supportsRequest( final DataRetrievalRequest req ) {
		return req instanceof BindingsRestrictedTriplePatternRequest || super.supportsRequest(req);
	}
}
