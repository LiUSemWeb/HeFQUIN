package se.liu.ida.hefquin.engine.federation.access.impl.iface;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFInterface;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;

public class TPFInterfaceImpl implements TPFInterface
{
	public boolean supportsTriplePatternRequests() {
		return true;
	}

	public boolean supportsBGPRequests() {
		return false;
	}

	public boolean supportsRequest( final DataRetrievalRequest req ) {
		return req instanceof TriplePatternRequest;
	}

}
