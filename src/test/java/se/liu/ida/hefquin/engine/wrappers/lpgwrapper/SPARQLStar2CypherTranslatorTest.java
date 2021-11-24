package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.DefaultConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryBuilder;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.SPARQLStar2CypherTranslatorImpl;

import static org.junit.Assert.assertEquals;

public class SPARQLStar2CypherTranslatorTest {

    final CypherVar v1 = new CypherVar("cpvar1");
    final CypherVar v2 = new CypherVar("cpvar2");

    final CypherVar ret1 = new CypherVar("ret1");
    final CypherVar ret2 = new CypherVar("ret2");
    final CypherVar ret3 = new CypherVar("ret3");

    final CypherVar src1 = new CypherVar("src1");
    final CypherVar edge1 = new CypherVar("edge1");
    final CypherVar tgt1 = new CypherVar("tgt1");

    final CypherVar a1 = new CypherVar("a1");
    final CypherVar a2 = new CypherVar("a2");
    final CypherVar a3 = new CypherVar("a3");
    final CypherVar a4 = new CypherVar("a4");
    final CypherVar a5 = new CypherVar("a5");
    final CypherVar a6 = new CypherVar("a6");
    final CypherVar a7 = new CypherVar("a7");
    final CypherVar a8 = new CypherVar("a8");

    final LPGNode node22 = new LPGNode("22", null, null);
    final LPGNode node23 = new LPGNode("23", null, null);

    @Test
    public void translateVarPropertyLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple tp = new Triple(Var.alloc("s"), conf.mapProperty("name"),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(tp), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                            .addMatch(new NodeMatchClause(v1))
                            .addCondition(new PropertyValueCondition(v1, "name", "Quentin Tarantino"))
                            .addReturn(new VariableReturnStatement(v1, ret1))
                            .build(),
                        new CypherQueryBuilder()
                            .addMatch(new EdgeMatchClause(src1, edge1, tgt1))
                            .addCondition(new PropertyValueCondition(edge1, "name", "Quentin Tarantino"))
                            .addReturn(new TripleMapReturnStatement(src1, edge1, tgt1, ret1))
                            .build())
                , translation);
    }

    @Test
    public void translateVarLabelClassTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.getLabel(), conf.mapNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new NodeMatchClause(v1))
                    .addCondition(new NodeLabelCondition(v1, "Person"))
                    .addReturn(new VariableReturnStatement(v1, ret1))
                    .build(),
                translation);
    }

    @Test
    public void translateVarRelationshipNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), conf.mapNode(node22));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new EdgeMatchClause(v1, a1, a2))
                    .addCondition(new NodeIDCondition(a2, "22"))
                    .addCondition(new EdgeLabelCondition(a1, "DIRECTED"))
                    .addReturn(new VariableReturnStatement(v1, ret1))
                    .build(),
                translation);
    }

    @Test
    public void translateNodeLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.getLabel(), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new NodeMatchClause(v1))
                    .addCondition(new NodeIDCondition(v1, "22"))
                    .addReturn(new LabelsReturnStatement(v1, ret1))
                    .build(),
                translation);
    }

    @Test
    public void translateNodePropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.mapProperty("name"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new NodeMatchClause(a1))
                    .addCondition(new NodeIDCondition(a1, "22"))
                    .addCondition(new PropertyEXISTSCondition(a1, "name"))
                    .addReturn(new PropertyValueReturnStatement(a1, "name", ret1))
                    .build(),
                translation);
    }

    @Test
    public void translateNodeRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new EdgeMatchClause(a1, a2, v1))
                    .addCondition(new NodeIDCondition(a1, "22"))
                    .addCondition(new EdgeLabelCondition(a2, "DIRECTED"))
                    .addReturn(new VariableReturnStatement(v1, ret1))
                    .build(),
                translation);
    }

    @Test
    public void translateNodeVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), conf.mapNode(node23));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new EdgeMatchClause(a1, v1, a2))
                    .addCondition(new NodeIDCondition(a1, "22"))
                    .addCondition(new NodeIDCondition(a2, "23"))
                    .addReturn(new VariableReturnStatement(v1, ret1))
                    .build(),
                translation);
    }

    @Test
    public void translateNodeVarLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple( conf.mapNode(node22), Var.alloc("p"),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new NodeMatchClause(a1))
                    .addCondition(new NodeIDCondition(a1, "22"))
                    .addReturn(new FilteredPropertiesReturnStatement(a1, "Quentin Tarantino", ret1))
                    .build(),
                translation);
    }

    @Test
    public void translateNodeVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), conf.mapNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                        .addMatch(new NodeMatchClause(a1))
                        .addCondition(new NodeIDCondition(a1, node22.getId()))
                        .addCondition(new NodeLabelCondition(a1, "Person"))
                        .addReturn(new LiteralValueReturnStatement("label", ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateVarLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.getLabel(), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new NodeMatchClause(v1))
                    .addReturn(new VariableReturnStatement(v1, ret1))
                    .addReturn(new LabelsReturnStatement(v1, ret2))
                    .build(),
                translation);
    }

    @Test
    public void translateVarRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new EdgeMatchClause(v1, a1, v2))
                    .addCondition(new EdgeLabelCondition(a1, "DIRECTED"))
                    .addReturn(new VariableReturnStatement(v1, ret1))
                    .addReturn(new VariableReturnStatement(v2, ret2))
                    .build(),
                translation);
    }

    @Test
    public void translateVarPropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapProperty("name"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .addMatch(new EdgeMatchClause(src1, edge1, tgt1))
                                .addCondition(new PropertyEXISTSCondition(edge1, "name"))
                                .addReturn(new TripleMapReturnStatement(src1, edge1, tgt1, ret1))
                                .addReturn(new PropertyValueReturnStatement(edge1, "name", ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(v1))
                                .addCondition(new PropertyEXISTSCondition(v1, "name"))
                                .addReturn(new VariableReturnStatement(v1, ret1))
                                .addReturn(new PropertyValueReturnStatement(v1, "name", ret2)).build()),
                translation);
    }

    @Test
    public void translateVarVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), conf.mapNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new NodeMatchClause(v1))
                    .addCondition(new NodeLabelCondition(v1, "Person"))
                    .addReturn(new VariableReturnStatement(v1, ret1))
                    .build(),
                translation);
    }

    @Test
    public void translateVarVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), conf.mapNode(node22));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherQueryBuilder()
                    .addMatch(new EdgeMatchClause(v1, v2, a1))
                    .addCondition(new NodeIDCondition(a1, "22"))
                    .addReturn(new VariableReturnStatement(v1, ret1))
                    .addReturn(new VariableReturnStatement(v2, ret2))
                    .build(),
                translation);
    }

    @Test
    public void translateVarVarLiteral() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), NodeFactory.createLiteral("The Matrix"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .addMatch(new EdgeMatchClause(src1, edge1, tgt1))
                                .addReturn(new TripleMapReturnStatement(src1, edge1, tgt1, ret1))
                                .addReturn(new FilteredPropertiesReturnStatement(edge1, "The Matrix", ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(v1))
                                .addReturn(new VariableReturnStatement(v1, ret1))
                                .addReturn(new FilteredPropertiesReturnStatement(v1, "The Matrix", ret2))
                                .build()),
                translation);
    }

    @Test
    public void translateNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(a1))
                                .addCondition(new NodeIDCondition(a1, "22"))
                                .addReturn(new LiteralValueReturnStatement("label", ret1))
                                .addReturn(new LabelsReturnStatement(a1, ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(a2))
                                .addCondition(new NodeIDCondition(a2, "22"))
                                .addReturn(new PropertyListReturnStatement(a2, ret1))
                                .addReturn(new AllPropertyValuesReturnStatement(a2, ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new EdgeMatchClause(a3, a4, a5))
                                .addCondition(new NodeIDCondition(a3, "22"))
                                .addReturn(new VariableReturnStatement(a4, ret1))
                                .addReturn(new VariableReturnStatement(a5, ret2))
                                .build()),
                translation);
    }

    @Test
    public void translateVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .addMatch(new EdgeMatchClause(a1, a2, a3))
                                .addReturn(new VariableReturnStatement(a1, ret1))
                                .addReturn(new VariableReturnStatement(a2, ret2))
                                .addReturn(new VariableReturnStatement(a3, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(a4))
                                .addReturn(new VariableReturnStatement(a4, ret1))
                                .addReturn(new LiteralValueReturnStatement("label", ret2))
                                .addReturn(new LabelsReturnStatement(a4, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(a5))
                                .addReturn(new VariableReturnStatement(a5, ret1))
                                .addReturn(new PropertyListReturnStatement(a5, ret2))
                                .addReturn(new AllPropertyValuesReturnStatement(a5, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new EdgeMatchClause(a6, a7, a8))
                                .addReturn(new TripleMapReturnStatement(a6, a7, a8, ret1))
                                .addReturn(new PropertyListReturnStatement(a7, ret2))
                                .addReturn(new AllPropertyValuesReturnStatement(a7, ret3))
                                .build()),
                translation);
    }

}
