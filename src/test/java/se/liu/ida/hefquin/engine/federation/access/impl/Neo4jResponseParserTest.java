package se.liu.ida.hefquin.engine.federation.access.impl;

import org.junit.Test;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
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

import java.util.Iterator;

import static org.junit.Assert.*;

public class Neo4jResponseParserTest extends EngineTestBase {

    @Test
    public void parseEdgeAndNodesTest() throws FederationAccessException {
        if (!skipLiveWebTests) {
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

    @Test
    public void parseLiteralsMapsAndArraysTest() throws FederationAccessException {
        if (!skipLiveWebTests) {
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

}
