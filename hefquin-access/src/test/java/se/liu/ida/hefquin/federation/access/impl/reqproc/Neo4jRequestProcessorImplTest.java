package se.liu.ida.hefquin.federation.access.impl.reqproc;

import org.junit.Test;

import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.Neo4jServer;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.federation.access.RecordsResponse;
import se.liu.ida.hefquin.federation.access.impl.req.Neo4jRequestImpl;

import static org.junit.Assert.assertEquals;

public class Neo4jRequestProcessorImplTest extends FederationTestBase {

    @Test
    public void testLocalhost() throws FederationAccessException {
        if ( ! skipLocalNeo4jTests ){
            final String cypherQuery = "MATCH (x)-[e:ACTED_IN]->(y) RETURN x, e, head(labels(y)) AS l, {a: 2} AS t LIMIT 2";
            final Neo4jRequest req = new Neo4jRequestImpl(cypherQuery);

            final Neo4jServer fm = new Neo4jServerImpl4Test();

            final Neo4jRequestProcessor processor = new Neo4jRequestProcessorImpl();

            final RecordsResponse response = processor.performRequest(req, fm);

            assertEquals( fm, response.getFederationMember() );
            assertEquals( req, response.getRequest() );
        }
    }

}
