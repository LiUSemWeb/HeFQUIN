package se.liu.ida.hefquin.federation.access.impl.iface;

import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

public class SPARQLEndpointInterfaceImpl implements SPARQLEndpointInterface
{
	public boolean supportsTriplePatternRequests() {
		return true;
	}

	public boolean supportsBGPRequests() {
		return true;
	}

	public boolean supportsRequest( final DataRetrievalRequest req ) {
		return req instanceof SPARQLRequest;
	}

}
