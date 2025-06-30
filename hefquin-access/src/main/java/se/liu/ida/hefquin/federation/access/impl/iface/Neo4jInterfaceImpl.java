package se.liu.ida.hefquin.federation.access.impl.iface;

import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.Neo4jInterface;
import se.liu.ida.hefquin.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.federation.access.impl.DataRetrievalInterfaceBase;

public class Neo4jInterfaceImpl extends DataRetrievalInterfaceBase implements Neo4jInterface
{
    protected final String url;

    public Neo4jInterfaceImpl( final String url ) {
        assert url != null;
        this.url = url;
    }

	@Override
	public boolean equals( final Object o ) {
		return o instanceof Neo4jInterface && ((Neo4jInterface) o).getURL().equals(url);
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
        return req instanceof Neo4jRequest;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String toString() {
        return "Neo4j interface at " + this.url;
    }

}
