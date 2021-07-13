package se.liu.ida.hefquin.engine.util;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.lpg.Configuration;
import se.liu.ida.hefquin.engine.utils.lpg.DefaultConfiguration;
import se.liu.ida.hefquin.engine.utils.lpg.SPARQLStar2CypherTranslator;

import static org.junit.Assert.assertEquals;

public class SPARQLStar2CypherTranslationTest {

    @Test
    public void testVariablePropertyLiteral() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapProperty("name")),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) WHERE cpvar1.name = 'Quentin Tarantino' " +
                        "RETURN nm(cpvar1) AS r1, '' AS r2, '' AS r3 UNION " +
                        "MATCH (cpvar2)-[cpvar3]->(cpvar4) WHERE cpvar2.name = 'Quentin Tarantino' " +
                        "RETURN nm(cpvar2) AS r1, elm(cpvar3) AS r2, nm(cpvar4) AS r3");
    }

    @Test
    public void testVarLabelClass() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.getLabelIRI()),
                NodeFactory.createURI(conf.mapLabel("Person")));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) WHERE cpvar1:Person RETURN nm(cpvar1) AS s");
    }

    @Test
    public void testVarRelationshipURI() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapRelationship("DIRECTED")),
                NodeFactory.createURI(conf.mapNode("22")));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1)-[:DIRECTED]->(cpvar2) " +
                        "WHERE ID(cpvar2) = 22 RETURN nm(cpvar1) AS s");
    }

    @Test
    public void testNodeLabelVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                NodeFactory.createURI(conf.getLabelIRI()),
                Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) WHERE ID(cpvar1)=22 RETURN labels(cpvar1) AS o");
    }

    @Test
    public void testNodePropVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                NodeFactory.createURI(conf.mapProperty("name")),
                Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) WHERE ID(cpvar1)=22 AND EXISTS(cpvar1.name) RETURN cpvar1.name AS o");
    }

    @Test
    public void testNodeRelVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                NodeFactory.createURI(conf.mapRelationship("DIRECTED")),
                Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1)-[:DIRECTED]->(cpvar2) WHERE ID(cpvar1)=22 RETURN nm(cpvar2) AS o");
    }

    @Test
    public void testNodeVarNode() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                Var.alloc("p"),
                NodeFactory.createURI(conf.mapNode("23")));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1)-[cpvar2]->(cpvar3) WHERE ID(cpvar1)=22 AND ID(cpvar3)=23 RETURN elm(cpvar2) AS p");
    }

    @Test
    public void testNodeVarLiteral() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                Var.alloc("p"),
                NodeFactory.createLiteral("Q. Tarantino"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) WHERE ID(cpvar1)=22 RETURN [k in KEYS(cpvar1) " +
                        "WHERE cpvar1[k]='Q. Tarantino' | pm(k)] AS p");
    }

    @Test
    public void testVarLabelVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.getLabelIRI()),
                Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) RETURN nm(cpvar1) AS s, labels(cpvar1) AS o");
    }

    @Test
    public void testNodeVarLabel() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                Var.alloc("p"),
                NodeFactory.createURI(conf.mapLabel("Person")));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "RETURN "+ conf.getLabelIRI() + " AS p");
    }

    @Test
    public void testVarRelationshipVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapRelationship("DIRECTED")),
                Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1)-[:DIRECTED]->(cpvar2) RETURN nm(cpvar1) AS s, nm(cpvar2) AS o");
    }

    @Test
    public void testVarPropertyVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapProperty("name")),
                Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) WHERE EXISTS(cpvar1.name) " +
                        "RETURN nm(cpvar1) AS r1, '' AS r2, '' AS r3, cpvar1.name AS o UNION " +
                        "MATCH (cpvar2)-[cpvar3]->(cpvar4) WHERE EXISTS(cpvar3.name) " +
                        "RETURN nm(cpvar2) AS r1, elm(cpvar3) AS r2, nm(cpvar4) AS r3, cpvar3.name AS o");
    }

    @Test
    public void testVarVarVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s1"), Var.alloc("p"), Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1)-[cpvar2]->(cpvar3) " +
                        "RETURN nm(cpvar1) AS s1, elm(cpvar2) AS p, nm(cpvar3) AS o UNION " +
                        "MATCH (cpvar4) RETURN nm(cpvar4) AS s1, "+ conf.getLabelIRI() +
                        " AS p, labels(cpvar4) AS o; " +
                        "MATCH (cpvar5) RETURN cpvar5 AS s UNION MATCH ()-[cpvar6]->() RETURN cpvar6 AS s");
    }

}
