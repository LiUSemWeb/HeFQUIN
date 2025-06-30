package se.liu.ida.hefquin.federation.access.impl.iface;

import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.GraphQLInterface;
import se.liu.ida.hefquin.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.federation.access.impl.DataRetrievalInterfaceBase;

public class GraphQLInterfaceImpl extends DataRetrievalInterfaceBase implements GraphQLInterface
{
	protected final String url;

	public GraphQLInterfaceImpl( final String url ) {
		assert url != null;
		this.url = url;
	}

	@Override
	public boolean supportsTriplePatternRequests() {
		return false;
	}

	@Override
	public boolean supportsBGPRequests() {
		return false;
	}

	@Override
	public boolean supportsSPARQLPatternRequests() {
		return false;
	}

	@Override
	public boolean supportsRequest( final DataRetrievalRequest req ) {
		return req instanceof GraphQLRequest;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public String toString() {
		return "GraphQL endpoint at <" + url + ">";
	}

}
