package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.response.StringResponseImpl;

import java.util.Date;

public class Neo4jRequestProcessorImpl implements Neo4jRequestProcessor{
    @Override
    public StringRetrievalResponse performRequest(Neo4jRequest req, Neo4jServer fm) {
        final Neo4jConnectionFactory.Neo4jConnection conn = Neo4jConnectionFactory.connect(fm.getInterface().getURL());
        final Date requestStartTime = new Date();
        String result = conn.executeQuery(req.getCypherQuery());
        return new StringResponseImpl(result, fm, req, requestStartTime);
    }
}
