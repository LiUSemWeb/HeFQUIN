package se.liu.ida.hefquin.engine.util;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.cypher.*;
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

    CypherVar v1 = new CypherVar("cpvar1");
    CypherVar v2 = new CypherVar("cpvar2");
    CypherVar v3 = new CypherVar("cpvar3");
    CypherVar v4 = new CypherVar("cpvar4");


    @Test
    public void testVariablePropertyLiteral() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapProperty("name")),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(new UnionCypherQuery(
                CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(v2, v4, v3, null))
                        .condition(new ValueWhereCondition(v3, "name", "Quentin Tarantino"))
                        .returns(new NodeMappingReturnStatement(v2, "r1"))
                        .returns(new EdgeMappingReturnStatement(v3, "r2"))
                        .returns(new NodeMappingReturnStatement(v4, "r3")).build(),
                CypherQueryBuilder.newBuilder().match(new NodeMatchClause(v1))
                        .condition(new ValueWhereCondition(v1, "name", "Quentin Tarantino"))
                        .returns(new NodeMappingReturnStatement(v1, "r1"))
                        .returns(new EmptyReturnStatement("r2"))
                        .returns(new EmptyReturnStatement("r3"))
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
                .match(new NodeMatchClause(v1))
                .condition(new ClassWhereCondition(v1, "Person"))
                .returns(new NodeMappingReturnStatement(v1, "s"))
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
                        .match(new EdgeMatchClause(v1, v3, v2, "DIRECTED"))
                        .condition(new IDWhereCondition(v3, "22"))
                        .returns(new NodeMappingReturnStatement(v1, "s"))
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
                .match(new NodeMatchClause(v1))
                .condition(new IDWhereCondition(v1, "22"))
                .returns(new LabelsReturnStatement(v1, "o"))
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
                .match(new NodeMatchClause(v1))
                .condition(new IDWhereCondition(v1, "22"))
                .condition(new EXISTSWhereCondition(v1, "name"))
                .returns(new LiteralReturnStatement(v1, "name", "o"))
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
                .match(new EdgeMatchClause(v1, v3, v2, "DIRECTED"))
                .condition(new IDWhereCondition(v1, "22"))
                .returns(new NodeMappingReturnStatement(v3, "o"))
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
                .match(new EdgeMatchClause(v1, v3, v2, null))
                .condition(new IDWhereCondition(v1, "22"))
                .condition(new IDWhereCondition(v3, "23"))
                .returns(new EdgeMappingReturnStatement(v2, "p"))
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
                .match(new NodeMatchClause(v1))
                .condition(new IDWhereCondition(v1, "22"))
                .returns(new FilteredPropertiesReturnStatement(v1, "p", "Q. Tarantino" ))
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
                .match(new NodeMatchClause(v1))
                .returns(new NodeMappingReturnStatement(v1, "s"))
                .returns(new LabelsReturnStatement(v1, "o"))
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
        assertEquals(CypherQueryBuilder.newBuilder().returns(new ValueReturnStatement(conf.getLabelIRI(),"p"))
                .build(), translation);
    }

    @Test
    public void testVarRelationshipVar() {
        final Configuration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(conf.mapRelationship("DIRECTED")),
                Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(CypherQueryBuilder.newBuilder()
                .match(new EdgeMatchClause(v1, v3, v2, "DIRECTED"))
                .returns(new NodeMappingReturnStatement(v1, "s"))
                .returns(new NodeMappingReturnStatement(v3, "o"))
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
                                .match(new NodeMatchClause(v1))
                                .condition(new EXISTSWhereCondition(v1, "name"))
                                .returns(new NodeMappingReturnStatement(v1, "r1"))
                                .returns(new EmptyReturnStatement("r2"))
                                .returns(new EmptyReturnStatement("r3"))
                                .returns(new LiteralReturnStatement(v1, "name", "o")).build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new EdgeMatchClause(v1, v3, v2, null))
                                .condition(new EXISTSWhereCondition(v2, "name"))
                                .returns(new NodeMappingReturnStatement(v1, "r1"))
                                .returns(new EdgeMappingReturnStatement(v2, "r2"))
                                .returns(new NodeMappingReturnStatement(v3, "r3"))
                                .returns(new LiteralReturnStatement(v2, "name", "o"))
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
                .match(new NodeMatchClause(v1))
                .condition(new ClassWhereCondition(v1, "Person"))
                .returns(new NodeMappingReturnStatement(v1, "s"))
                .returns(new ValueReturnStatement(conf.getLabelIRI(),"p"))
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
                .match(new EdgeMatchClause(v1, v3, v2, null))
                .condition(new IDWhereCondition(v3, "22"))
                .returns(new NodeMappingReturnStatement(v1, "s"))
                .returns(new EdgeMappingReturnStatement(v2, "p"))
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
                                .match(new EdgeMatchClause(v1, v3, v2, null))
                                .returns(new NodeMappingReturnStatement(v1, "r1"))
                                .returns(new EdgeMappingReturnStatement(v2, "r2"))
                                .returns(new NodeMappingReturnStatement(v3, "r3"))
                                .returns(new FilteredPropertiesReturnStatement(v2, "p", "The Matrix"))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(v1))
                                .returns(new NodeMappingReturnStatement(v1, "r1"))
                                .returns(new EmptyReturnStatement("r2"))
                                .returns(new EmptyReturnStatement("r3"))
                                .returns(new FilteredPropertiesReturnStatement(v1, "p", "The Matrix"))
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
                                .match(new NodeMatchClause(v1))
                                .condition(new IDWhereCondition(v1, "22"))
                                .returns(new ValueReturnStatement(conf.getLabelIRI(), "p"))
                                .returns(new LabelsReturnStatement(v1, "o"))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(v1))
                                .condition(new IDWhereCondition(v1, "22"))
                                .returns(new PropertyListReturnStatement(v1, "p"))
                                .returns(new PropertyValuesReturnStatement(v1, "o"))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new EdgeMatchClause(v1, v3, v2, null))
                                .condition(new IDWhereCondition(v1, "22"))
                                .returns(new EdgeMappingReturnStatement(v2, "p"))
                                .returns(new NodeMappingReturnStatement(v3, "o"))
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
                                .match(new EdgeMatchClause(v1, v3, v2, null))
                                .returns(new NodeMappingReturnStatement(v1, "r1"))
                                .returns(new EmptyReturnStatement("r2"))
                                .returns(new EmptyReturnStatement("r3"))
                                .returns(new EdgeMappingReturnStatement(v2, "p"))
                                .returns(new NodeMappingReturnStatement(v3, "o"))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(v1))
                                .returns(new NodeMappingReturnStatement(v1, "r1"))
                                .returns(new EmptyReturnStatement("r2"))
                                .returns(new EmptyReturnStatement("r3"))
                                .returns(new ValueReturnStatement(conf.getLabelIRI(), "p"))
                                .returns(new LabelsReturnStatement(v1, "o"))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(v1))
                                .returns(new NodeMappingReturnStatement(v1, "r1"))
                                .returns(new EmptyReturnStatement("r2"))
                                .returns(new EmptyReturnStatement("r3"))
                                .returns(new PropertyListReturnStatement(v1, "p"))
                                .returns(new PropertyValuesReturnStatement(v1, "o"))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new EdgeMatchClause(v1, v3, v2, null))
                                .returns(new NodeMappingReturnStatement(v1, "r1"))
                                .returns(new EdgeMappingReturnStatement(v2, "r2"))
                                .returns(new NodeMappingReturnStatement(v3, "r3"))
                                .returns(new PropertyListReturnStatement(v2, "p"))
                                .returns(new PropertyValuesReturnStatement(v2, "o"))
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
        assertEquals(CypherQueryBuilder.newBuilder()
                .match(new EdgeMatchClause(v1, v3, v2, "DIRECTED"))
                .condition(new EXISTSWhereCondition(v2, "year"))
                .condition(new IDWhereCondition(v1, "22"))
                .returns(new NodeMappingReturnStatement(v3, "o1"))
                .returns(new LiteralReturnStatement(v2, "year", "o"))
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
        assertEquals(CypherQueryBuilder.newBuilder()
                .match(new EdgeMatchClause(v1, v3, v2, "DIRECTED"))
                .condition(new EXISTSWhereCondition(v2, "year"))
                .condition(new IDWhereCondition(v1, "22"))
                .condition(new IDWhereCondition(v3, "23"))
                .returns(new LiteralReturnStatement(v2, "year", "o"))
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
                        .match(new EdgeMatchClause(v1, v3, v2, "DIRECTED"))
                        .condition(new ClassWhereCondition(v1, "Person"))
                        .condition(new ClassWhereCondition(v3, "Movie"))
                        .condition(new ValueWhereCondition(v1, "name", "Q. Tarantino"))
                        .condition(new ValueWhereCondition(v2, "retrievedFrom", "IMDB"))
                        .condition(new EXISTSWhereCondition(v3, "released"))
                        .returns(new NodeMappingReturnStatement(v1, "p"))
                        .returns(new NodeMappingReturnStatement(v3, "m"))
                        .returns(new LiteralReturnStatement(v3, "released", "year"))
                        .build())
                        || translation.equals(CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(v1, v2, v3, "DIRECTED"))
                        .condition(new ClassWhereCondition(v1, "Person"))
                        .condition(new ClassWhereCondition(v2, "Movie"))
                        .condition(new ValueWhereCondition(v1, "name", "Q. Tarantino"))
                        .condition(new ValueWhereCondition(v3, "retrievedFrom", "IMDB"))
                        .condition(new EXISTSWhereCondition(v2, "released"))
                        .returns(new NodeMappingReturnStatement(v1, "p"))
                        .returns(new NodeMappingReturnStatement(v2, "m"))
                        .returns(new LiteralReturnStatement(v2, "released", "year"))
                        .build())
                        || translation.equals(CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(v2, v1, v3, "DIRECTED"))
                        .condition(new ClassWhereCondition(v2, "Person"))
                        .condition(new ClassWhereCondition(v1, "Movie"))
                        .condition(new ValueWhereCondition(v2, "name", "Q. Tarantino"))
                        .condition(new ValueWhereCondition(v3, "retrievedFrom", "IMDB"))
                        .condition(new EXISTSWhereCondition(v1, "released"))
                        .returns(new NodeMappingReturnStatement(v2, "p"))
                        .returns(new NodeMappingReturnStatement(v1, "m"))
                        .returns(new LiteralReturnStatement(v1, "released", "year"))
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
        assertEquals(new UnionCypherQuery(
                CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(v1))
                        .condition(new EXISTSWhereCondition(v1, "name"))
                        .condition(new EXISTSWhereCondition(v1, "year"))
                        .returns(new NodeMappingReturnStatement(v1, "r1"))
                        .returns(new EmptyReturnStatement("r2"))
                        .returns(new EmptyReturnStatement("r3"))
                        .returns(new LiteralReturnStatement(v1, "name", "name"))
                        .returns(new LiteralReturnStatement(v1, "year", "year"))
                        .build(),
                CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(v1, v3, v2, null))
                        .condition(new EXISTSWhereCondition(v2, "name"))
                        .condition(new EXISTSWhereCondition(v2, "year"))
                        .returns(new NodeMappingReturnStatement(v1, "r1"))
                        .returns(new EdgeMappingReturnStatement(v2, "r2"))
                        .returns(new NodeMappingReturnStatement(v3, "r3"))
                        .returns(new LiteralReturnStatement(v2, "name", "name"))
                        .returns(new LiteralReturnStatement(v2, "year", "year"))
                        .build()
        ), translation);
    }

}
