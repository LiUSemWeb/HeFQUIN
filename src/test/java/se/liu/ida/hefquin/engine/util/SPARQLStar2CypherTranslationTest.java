package se.liu.ida.hefquin.engine.util;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.utils.CypherQueryBuilder;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.query.impl.UnionCypherQuery;
import se.liu.ida.hefquin.engine.utils.lpg.Configuration;
import se.liu.ida.hefquin.engine.utils.lpg.DefaultConfiguration;
import se.liu.ida.hefquin.engine.utils.lpg.SPARQLStar2CypherTranslator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SPARQLStar2CypherTranslationTest {

    @Test
    public void testVariablePropertyLiteral() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapProperty("name")),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(new UnionCypherQuery(
                CypherQueryBuilder.newBuilder()
                        .match("MATCH (cpvar2)-[cpvar3]->(cpvar4)")
                        .condition("cpvar3.name='Quentin Tarantino'")
                        .returns("nm(cpvar2) AS r1")
                        .returns("elm(cpvar3) AS r2")
                        .returns("nm(cpvar4) AS r3").build(),
                CypherQueryBuilder.newBuilder().match("MATCH (cpvar1)")
                        .condition("cpvar1.name='Quentin Tarantino'")
                        .returns("nm(cpvar1) AS r1")
                        .returns("'' AS r2")
                        .returns("'' AS r3")
                        .build()), translation);
    }

    @Test
    public void testVarLabelClass() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.getLabelIRI()),
                NodeFactory.createURI(conf.mapLabel("Person")));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)")
                .condition("cpvar1:Person").returns("nm(cpvar1) AS s")
                .build(), translation);
    }

    @Test
    public void testVarRelationshipNode() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapRelationship("DIRECTED")),
                NodeFactory.createURI(conf.mapNode("22")));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                        .match("MATCH (cpvar1)-[cpvar2:DIRECTED]->(cpvar3)")
                        .condition("ID(cpvar3)=22")
                        .returns("nm(cpvar1) AS s")
                .build(), translation);
    }

    @Test
    public void testNodeLabelVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                NodeFactory.createURI(conf.getLabelIRI()),
                Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)")
                .condition("ID(cpvar1)=22")
                .returns("labels(cpvar1) AS o")
                .build(), translation);
    }

    @Test
    public void testNodePropVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                NodeFactory.createURI(conf.mapProperty("name")),
                Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)")
                .condition("ID(cpvar1)=22")
                .condition("EXISTS(cpvar1.name)")
                .returns("cpvar1.name AS o")
                .build(), translation);
    }

    @Test
    public void testNodeRelVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                NodeFactory.createURI(conf.mapRelationship("DIRECTED")),
                Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)-[cpvar2:DIRECTED]->(cpvar3)")
                .condition("ID(cpvar1)=22")
                .returns("nm(cpvar3) AS o")
                .build(), translation);
    }

    @Test
    public void testNodeVarNode() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                Var.alloc("p"),
                NodeFactory.createURI(conf.mapNode("23")));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)-[cpvar2]->(cpvar3)")
                .condition("ID(cpvar1)=22")
                .condition("ID(cpvar3)=23")
                .returns("elm(cpvar2) AS p")
                .build(), translation);
    }

    @Test
    public void testNodeVarLiteral() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                Var.alloc("p"),
                NodeFactory.createLiteral("Q. Tarantino"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)")
                .condition("ID(cpvar1)=22")
                .returns("[k IN KEYS(cpvar1) WHERE cpvar1[k]='Q. Tarantino' | pm(k)] AS p")
                .build(), translation);
    }

    @Test
    public void testVarLabelVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.getLabelIRI()),
                Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)")
                .returns("nm(cpvar1) AS s")
                .returns("labels(cpvar1) AS o")
                .build(), translation);
    }

    @Test
    public void testNodeVarLabel() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                Var.alloc("p"),
                NodeFactory.createURI(conf.mapLabel("Person")));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder().returns(conf.getLabelIRI() + " AS p").build(), translation);
    }

    @Test
    public void testVarRelationshipVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapRelationship("DIRECTED")),
                Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)-[cpvar2:DIRECTED]->(cpvar3)")
                .returns("nm(cpvar1) AS s")
                .returns("nm(cpvar3) AS o")
                .build(), translation);
    }

    @Test
    public void testVarPropertyVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapProperty("name")),
                Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(
                new UnionCypherQuery(
                CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)")
                                .condition("EXISTS(cpvar1.name)")
                                .returns("nm(cpvar1) AS r1")
                                .returns("'' AS r2")
                                .returns("'' AS r3")
                                .returns("cpvar1.name AS o").build(),
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)-[cpvar2]->(cpvar3)")
                                .condition("EXISTS(cpvar2.name)")
                                .returns("nm(cpvar1) AS r1")
                                .returns("elm(cpvar2) AS r2")
                                .returns("nm(cpvar3) AS r3")
                                .returns("cpvar2.name AS o")
                                .build()
                ), translation);
    }

    @Test
    public void testVarVarLabel() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                Var.alloc("p"),
                NodeFactory.createURI(conf.mapLabel("Person")));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)")
                .condition("cpvar1:Person")
                .returns("nm(cpvar1) AS s")
                .returns(conf.getLabelIRI()+" AS p")
                .build(), translation);
    }

    @Test
    public void testVarVarNode() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                Var.alloc("p"),
                NodeFactory.createURI(conf.mapNode("22")));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match("MATCH (cpvar1)-[cpvar2]->(cpvar3)")
                .condition("ID(cpvar3)=22")
                .returns("nm(cpvar1) AS s")
                .returns("elm(cpvar2) AS p")
                .build(), translation);
    }

    @Test
    public void testVarVarLiteral() {
        final Triple t = new Triple(Var.alloc("s"),
                Var.alloc("p"),
                NodeFactory.createLiteral("The Matrix"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(
                new UnionCypherQuery(
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)-[cpvar2]->(cpvar3)")
                                .returns("nm(cpvar1) AS r1")
                                .returns("elm(cpvar2) AS r2")
                                .returns("nm(cpvar3) AS r3")
                                .returns("[k IN KEYS(cpvar2) WHERE cpvar2[k]='The Matrix' | pm(k)] AS p")
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)")
                                .returns("nm(cpvar1) AS r1")
                                .returns("'' AS r2")
                                .returns("'' AS r3")
                                .returns("[k IN KEYS(cpvar1) WHERE cpvar1[k]='The Matrix' | pm(k)] AS p")
                                .build()
                ), translation);
    }

    @Test
    public void testNodeVarVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(NodeFactory.createURI(conf.mapNode("22")),
                Var.alloc("p"),
                Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(
                new UnionCypherQuery(
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)")
                                .condition("ID(cpvar1)=22")
                                .returns(conf.getLabelIRI()+" AS p")
                                .returns("labels(cpvar1) AS o")
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)")
                                .condition("ID(cpvar1)=22")
                                .returns("[k IN KEYS(cpvar1) | pm(k)] AS p")
                                .returns("[k IN KEYS(cpvar1) | cpvar1[k]] AS o")
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)-[cpvar2]->(cpvar3)")
                                .condition("ID(cpvar1)=22")
                                .returns("elm(cpvar2) AS p")
                                .returns("nm(cpvar3) AS o")
                                .build()
                ), translation);
    }

    @Test
    public void testVarVarVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(
                new UnionCypherQuery(
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)-[cpvar2]->(cpvar3)")
                                .returns("nm(cpvar1) AS r1")
                                .returns("'' AS r2")
                                .returns("'' AS r3")
                                .returns("elm(cpvar2) AS p")
                                .returns("nm(cpvar3) AS o")
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)")
                                .returns("nm(cpvar1) AS r1")
                                .returns("'' AS r2")
                                .returns("'' AS r3")
                                .returns(conf.getLabelIRI()+" AS p")
                                .returns("labels(cpvar1) AS o")
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)")
                                .returns("nm(cpvar1) AS r1")
                                .returns("'' AS r2")
                                .returns("'' AS r3")
                                .returns("[k IN KEYS(cpvar1) | pm(k)] AS p")
                                .returns("[k IN KEYS(cpvar1) | cpvar1[k]] AS o")
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match("MATCH (cpvar1)-[cpvar2]->(cpvar3)")
                                .returns("nm(cpvar1) AS r1")
                                .returns("elm(cpvar2) AS r2")
                                .returns("nm(cpvar3) AS r3")
                                .returns("[k IN KEYS(cpvar2) | pm(k)] AS p")
                                .returns("[k IN KEYS(cpvar2) | cpvar2[k]] AS o")
                                .build()
                ), translation);
    }

    @Test
    public void testNested1() {
        final Configuration conf = new DefaultConfiguration();
        final Triple tp = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                NodeFactory.createURI(conf.mapRelationship("DIRECTED")),
                Var.alloc("o1")
        );
        final Triple t = new Triple(NodeFactory.createTripleNode(tp),
                NodeFactory.createURI(conf.mapProperty("year")), Var.alloc("o"));
        CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder().match("MATCH (cpvar1)-[cpvar2:DIRECTED]->(cpvar3)")
                .condition("EXISTS(cpvar2.year)")
                .condition("ID(cpvar1)=22")
                .returns("nm(cpvar3) AS o1")
                .returns("cpvar2.year AS o")
                .build(), translation);
    }

    @Test
    public void testNested2() {
        final Configuration conf = new DefaultConfiguration();
        final Triple tp = new Triple(
                NodeFactory.createURI(conf.mapNode("22")),
                NodeFactory.createURI(conf.mapRelationship("DIRECTED")),
                NodeFactory.createURI(conf.mapNode("23"))
        );
        final Triple t = new Triple(NodeFactory.createTripleNode(tp),
                NodeFactory.createURI(conf.mapProperty("year")), Var.alloc("o"));
        CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder().match("MATCH (cpvar1)-[cpvar2:DIRECTED]->(cpvar3)")
                .condition("EXISTS(cpvar2.year)")
                .condition("ID(cpvar1)=22")
                .condition("ID(cpvar3)=23")
                .returns("cpvar2.year AS o")
                .build(), translation);
    }

    @Test
    public void testBGPTranslation() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t1 = new Triple(
                Var.alloc("m"), NodeFactory.createURI(conf.getLabelIRI()),
                NodeFactory.createURI(conf.mapLabel("Movie"))
        );
        final Triple t2 = new Triple(
                Var.alloc("p"), NodeFactory.createURI(conf.getLabelIRI()),
                NodeFactory.createURI(conf.mapLabel("Person"))
        );
        final Triple t3 = new Triple(
                Var.alloc("p"), NodeFactory.createURI(conf.mapProperty("name")),
                NodeFactory.createLiteral("Q. Tarantino")
        );
        final Triple tp = new Triple(
                Var.alloc("p"), NodeFactory.createURI(conf.mapRelationship("DIRECTED")), Var.alloc("m")
        );
        final Triple t4 = new Triple(
                NodeFactory.createTripleNode(tp), NodeFactory.createURI(conf.mapProperty("retrievedFrom")),
                NodeFactory.createLiteral("IMDB")
        );
        final Triple t5 = new Triple(
                Var.alloc("m"), NodeFactory.createURI(conf.mapProperty("released")), Var.alloc("year")
        );
        final BGP bgp = new BGPImpl(
                new TriplePatternImpl(t1),
                new TriplePatternImpl(t2),
                new TriplePatternImpl(t3),
                new TriplePatternImpl(t4),
                new TriplePatternImpl(t5)
        );
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(bgp);
        assertTrue(
                translation.equals(CypherQueryBuilder.newBuilder()
                        .match("MATCH (cpvar1)-[cpvar2:DIRECTED]->(cpvar3)")
                        .match("MATCH (cpvar1)") // this two are redundant
                        .match("MATCH (cpvar3)")
                        .condition("cpvar1:Person")
                        .condition("cpvar3:Movie")
                        .condition("cpvar1.name='Q. Tarantino'")
                        .condition("cpvar2.retrievedFrom='IMDB'")
                        .condition("EXISTS(cpvar3.released)")
                        .returns("nm(cpvar1) AS p")
                        .returns("nm(cpvar3) AS m")
                        .returns("cpvar3.released AS year")
                        .build())
                        || translation.equals(CypherQueryBuilder.newBuilder()
                        .match("MATCH (cpvar1)-[cpvar3:DIRECTED]->(cpvar2)")
                        .match("MATCH (cpvar1)") // this two are redundant
                        .match("MATCH (cpvar2)")
                        .condition("cpvar1:Person")
                        .condition("cpvar2:Movie")
                        .condition("cpvar1.name='Q. Tarantino'")
                        .condition("cpvar3.retrievedFrom='IMDB'")
                        .condition("EXISTS(cpvar2.released)")
                        .returns("nm(cpvar1) AS p")
                        .returns("nm(cpvar2) AS m")
                        .returns("cpvar2.released AS year")
                        .build())
                        || translation.equals(CypherQueryBuilder.newBuilder()
                        .match("MATCH (cpvar2)-[cpvar3:DIRECTED]->(cpvar1)")
                        .match("MATCH (cpvar1)") // this two are redundant
                        .match("MATCH (cpvar2)")
                        .condition("cpvar2:Person")
                        .condition("cpvar1:Movie")
                        .condition("cpvar2.name='Q. Tarantino'")
                        .condition("cpvar3.retrievedFrom='IMDB'")
                        .condition("EXISTS(cpvar1.released)")
                        .returns("nm(cpvar2) AS p")
                        .returns("nm(cpvar1) AS m")
                        .returns("cpvar1.released AS year")
                        .build()));
    }

    @Test
    public void testBGPTranslation2() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t1 = new Triple(
                Var.alloc("s"), NodeFactory.createURI(conf.mapProperty("name")), Var.alloc("name")
        );
        final Triple t2 = new Triple(
                Var.alloc("s"), NodeFactory.createURI(conf.mapProperty("year")), Var.alloc("year")
        );
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t1),
                new TriplePatternImpl(t2)));
        System.out.println(translation);
    }

}
