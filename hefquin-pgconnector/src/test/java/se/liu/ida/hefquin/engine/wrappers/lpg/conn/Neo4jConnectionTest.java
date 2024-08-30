package se.liu.ida.hefquin.engine.wrappers.lpg.conn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.lpg.Neo4jException;
import se.liu.ida.hefquin.engine.wrappers.lpg.conn.Neo4jConnectionFactory.Neo4jConnection;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGEdgeValue;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNodeValue;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LiteralValue;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.MapValue;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.CypherVar;

public class Neo4jConnectionTest
{
	/**
	 * If this flag is true, tests that make requests to local neo4j
	 * instances will be skipped.
	 */
	public static boolean skipLocalNeo4jTests = true;
	public static String localNeo4jURI = "http://localhost:7474/db/neo4j/tx/commit";

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
    public void parseEdgeAndNodesTest() throws Neo4jException {
        if (!skipLocalNeo4jTests) {
            final String cypherQuery = "MATCH (x)-[e:ACTED_IN]->(y) RETURN x, e, y LIMIT 2";
            final Neo4jConnection conn = Neo4jConnectionFactory.connect(localNeo4jURI);
            final List<TableRecord> result = conn.execute(cypherQuery);

            assertEquals( 2, result.size() );

            for ( final TableRecord rec : result ) {
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
    public void parseLiteralsMapsAndArraysTest() throws Neo4jException {
        if (!skipLocalNeo4jTests) {
            final String cypherQuery = "RETURN 2 AS a, {x:1, y:2} AS c";
            final Neo4jConnection conn = Neo4jConnectionFactory.connect(localNeo4jURI);
            final List<TableRecord> result = conn.execute(cypherQuery);

            assertEquals( 1, result.size() );

            for ( final TableRecord rec : result ) {
                Iterator<RecordEntry> iterator = rec.getRecordEntries().iterator();
                assertTrue(iterator.hasNext());
                final RecordEntry a = iterator.next();
                assertEquals(new CypherVar("a"), a.getName());
                assertTrue(a.getValue() instanceof LiteralValue);
                assertTrue(iterator.hasNext());
                final RecordEntry c = iterator.next();
                assertEquals(new CypherVar("c"), c.getName());
                assertTrue(c.getValue() instanceof MapValue);
                assertFalse(iterator.hasNext());
            }
        }
    }

}
