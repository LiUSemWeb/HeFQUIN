package se.liu.ida.hefquin.engine.federation.access.impl.iface;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.Neo4jInterface;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;

public class Neo4jInterfaceImpl implements Neo4jInterface {

    final String url;

    public Neo4jInterfaceImpl(final String url) {
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
    public boolean supportsRequest(DataRetrievalRequest req) {
        return req instanceof Neo4jRequest;
    }

    @Override
    public String getURL() {
        return url;
    }
}
