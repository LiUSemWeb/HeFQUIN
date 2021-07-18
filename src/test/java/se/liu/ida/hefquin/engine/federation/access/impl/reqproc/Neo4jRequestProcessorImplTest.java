package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import org.junit.Test;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.StringResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.Neo4jRequestImpl;

import static org.junit.Assert.assertEquals;

public class Neo4jRequestProcessorImplTest extends EngineTestBase {

    @Test
    public void testLocalhost() throws FederationAccessException {
        if ( ! skipLiveWebTests ){
            final String cypherQuery = "MATCH (x)-[:DIRECTED]->(y) RETURN x, y";
            final Neo4jRequest req = new Neo4jRequestImpl(cypherQuery);

            final Neo4jServer fm = () -> new Neo4jInterfaceImpl("http://localhost:7474/db/neo4j/tx");

            final Neo4jRequestProcessor processor = new Neo4jRequestProcessorImpl();

            final StringResponse response = processor.performRequest(req, fm);

            assertEquals( fm, response.getFederationMember() );
            assertEquals( req, response.getRequest() );
        }
    }

}
