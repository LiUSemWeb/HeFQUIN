package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.Neo4JException;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.RecordsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.Neo4jRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherUtils;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class CypherUtilsTest extends EngineTestBase {

    /**
     * This test makes use of the example Movie database provided in most neo4j clients.
     * The database has two types of nodes: Person and Movie.
     * The database has two types of relationships: DIRECTED and ACTED_IN
     *
     * cf. https://neo4j.com/developer/example-data/
     *
     * @throws FederationAccessException
     */
    @Test
    public void parseEdgeAndNodesTest() throws FederationAccessException {
        if (!skipLocalNeo4jTests) {
            final String cypherQuery = "MATCH (x)-[e:ACTED_IN]->(y) RETURN x, e, y LIMIT 2";
            final Neo4jRequest req = new Neo4jRequestImpl(cypherQuery);

            final Neo4jServer fm = () -> new Neo4jInterfaceImpl("http://localhost:7474/db/neo4j/tx");

            final Neo4jRequestProcessor processor = new Neo4jRequestProcessorImpl();

            final RecordsResponse response = processor.performRequest(req, fm);

            assertEquals(2, response.getResponse().size());
            for (final TableRecord rec : response.getResponse()) {
                Iterator<RecordEntry> iterator = rec.getRecordEntries().iterator();
                assertTrue(iterator.hasNext());
                final RecordEntry nodeX = iterator.next();
                assertEquals(new CypherVar("x"), nodeX.getName());
                assertTrue(nodeX.getValue() instanceof LPGNodeValue);
                assertTrue(iterator.hasNext());
                final RecordEntry edgeE = iterator.next();
                assertEquals(new CypherVar("e"), edgeE.getName());
                assertTrue(edgeE.getValue() instanceof LPGEdgeValue);
                assertTrue(iterator.hasNext());
                final RecordEntry nodeY = iterator.next();
                assertEquals(new CypherVar("y"), nodeY.getName());
                assertTrue(nodeY.getValue() instanceof LPGNodeValue);
                assertFalse(iterator.hasNext());
            }
        }
    }

    /**
     * This test does not use a specific dataset, since the Cypher query returns hardcoded literals.
     * It is, however, necessary to have a local neo4j instance running.
     *
     * @throws FederationAccessException
     */
    @Test
    public void parseLiteralsMapsAndArraysTest() throws FederationAccessException {
        if (!skipLocalNeo4jTests) {
            final String cypherQuery = "RETURN 2 AS a, [1, 2, 3] AS b, {x:1, y:2} AS c";
            final Neo4jRequest req = new Neo4jRequestImpl(cypherQuery);

            final Neo4jServer fm = () -> new Neo4jInterfaceImpl("http://localhost:7474/db/neo4j/tx");

            final Neo4jRequestProcessor processor = new Neo4jRequestProcessorImpl();

            final RecordsResponse response = processor.performRequest(req, fm);

            assertEquals(1, response.getResponse().size());
            for (final TableRecord rec : response.getResponse()) {
                Iterator<RecordEntry> iterator = rec.getRecordEntries().iterator();
                assertTrue(iterator.hasNext());
                final RecordEntry a = iterator.next();
                assertEquals(new CypherVar("a"), a.getName());
                assertTrue(a.getValue() instanceof LiteralValue);
                assertTrue(iterator.hasNext());
                final RecordEntry b = iterator.next();
                assertEquals(new CypherVar("b"), b.getName());
                assertTrue(b.getValue() instanceof ListValue);
                assertTrue(iterator.hasNext());
                final RecordEntry c = iterator.next();
                assertEquals(new CypherVar("c"), c.getName());
                assertTrue(c.getValue() instanceof MapValue);
                assertFalse(iterator.hasNext());
            }
        }
    }

    @Test
    public void parseNodesFromStringTest() throws JsonProcessingException, Neo4JException {
        final String response = "{\"results\": [" +
                "{\"columns\": [\"x\", \"y\", \"z\"]," +
                "\"data\": [{" +
                    "\"row\": [" +
                        "{\"p1\": \"v1\", \"p2\": \"v2\"}," +
                        "{\"p1\": \"v3\", \"p2\": \"v4\"}," +
                        "{\"p1\": \"v5\", \"p2\": \"v6\"}" +
                    "]," +
                    "\"meta\": [" +
                        "{\"id\": 1, \"type\": \"node\", \"deleted\": false}," +
                        "{\"id\": 2, \"type\": \"node\", \"deleted\": false}," +
                        "{\"id\": 3, \"type\": \"node\", \"deleted\": false}" +
                    "]}" +
                "]}], \"errors\": []}";
        List<TableRecord> records = CypherUtils.parse(response);
        assertEquals(1, records.size());
        final Iterator<RecordEntry> entries =  records.get(0).getRecordEntries().iterator();
        assertTrue(entries.hasNext());
        final RecordEntry x = entries.next();
        assertEquals(new CypherVar("x"), x.getName());
        assertTrue(x.getValue() instanceof LPGNodeValue);
        assertTrue(entries.hasNext());
        final RecordEntry y = entries.next();
        assertEquals(new CypherVar("y"), y.getName());
        assertTrue(y.getValue() instanceof LPGNodeValue);
        assertTrue(entries.hasNext());
        final RecordEntry z = entries.next();
        assertEquals(new CypherVar("z"), z.getName());
        assertTrue(z.getValue() instanceof LPGNodeValue);
    }

    @Test
    public void parseMiscTest() throws JsonProcessingException, Neo4JException {
        final String response = "{\"results\": [" +
                "{\"columns\": [\"x\", \"y\", \"z\"]," +
                    "\"data\": [{" +
                        "\"row\": [" +
                            "{\"p1\": \"v1\", \"p2\": \"v2\"}," +
                            "{\"p1\": \"v3\", \"p2\": \"v4\"}," +
                            "4" +
                        "]," +
                        "\"meta\": [" +
                            "{\"id\": 1, \"type\": \"node\", \"deleted\": false}," +
                            "{\"id\": 2, \"type\": \"relationship\", \"deleted\": false}," +
                            "{}" +
                        "]}" +
                "]}], \"errors\": []}";
        List<TableRecord> records = CypherUtils.parse(response);
        assertEquals(1, records.size());
        final Iterator<RecordEntry> entries =  records.get(0).getRecordEntries().iterator();
        assertTrue(entries.hasNext());
        final RecordEntry x = entries.next();
        assertEquals(new CypherVar("x"), x.getName());
        assertTrue(x.getValue() instanceof LPGNodeValue);
        assertTrue(entries.hasNext());
        final RecordEntry y = entries.next();
        assertEquals(new CypherVar("y"), y.getName());
        assertTrue(y.getValue() instanceof LPGEdgeValue);
        assertTrue(entries.hasNext());
        final RecordEntry z = entries.next();
        assertEquals(new CypherVar("z"), z.getName());
        assertTrue(z.getValue() instanceof LiteralValue);
    }

    @Test(expected = JsonProcessingException.class)
    public void malformedJSONparseTest() throws JsonProcessingException, Neo4JException {
        CypherUtils.parse("{\"notResults\"}");
    }

}
