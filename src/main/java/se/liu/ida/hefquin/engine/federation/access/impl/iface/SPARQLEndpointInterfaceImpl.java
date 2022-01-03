package se.liu.ida.hefquin.engine.federation.access.impl.iface;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;

public class SPARQLEndpointInterfaceImpl implements SPARQLEndpointInterface
{
	protected final String url;

	public SPARQLEndpointInterfaceImpl( final String url ) {
		assert url != null;
		this.url = url;
	}

	@Override
	public boolean supportsTriplePatternRequests() {
		return true;
	}

	@Override
	public boolean supportsBGPRequests() {
		return true;
	}

	@Override
	public boolean supportsRequest( final DataRetrievalRequest req ) {
		return req instanceof SPARQLRequest;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof SPARQLEndpointInterface
				&& ((SPARQLEndpointInterface) o).getURL().equals(url);
	}

	@Override
	public String toString() {
		return "SPARQL endpoint at " + this.url;
	}

}
