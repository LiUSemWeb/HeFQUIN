package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.*;

import java.io.IOException;

public class Neo4jRequestProcessorImpl implements Neo4jRequestProcessor{
    @Override
    public SolMapsResponse performRequest(Neo4jRequest req, Neo4jServer fm) {
        final Neo4jConnection conn = Neo4jConnectionFactory.connect(fm.getInterface().getURL());
        String result = null;
        try {
            result = conn.executeQuery(req.getCypherQuery());
        } catch (InterruptedException | IOException e) {
            throw new Neo4JConnectionException("Neo4j server could not be reached.");
        }
        return toMapping(result);
    }

    private SolMapsResponse toMapping(String result) {
        return null;
    }
}
