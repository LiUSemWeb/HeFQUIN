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
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.SPARQLStar2CypherTranslator;

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
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(tp), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                    CypherQueryBuilder.newBuilder().match(new NodeMatchClause(v1))
                            .condition(new PropertyValueCondition(v1, "name", "Quentin Tarantino"))
                            .returns(new VariableReturnStatement(v1, ret1))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(new EdgeMatchClause(src1, edge1, tgt1))
                            .condition(new PropertyValueCondition(edge1, "name", "Quentin Tarantino"))
                            .returns(new TripleMapReturnStatement(src1, edge1, tgt1, ret1))
                            .build()), translation);
    }

    @Test
    public void translateVarLabelClassTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.getLabel(), conf.mapNodeLabel("Person"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(v1))
                    .condition(new NodeLabelCondition(v1, "Person"))
                    .returns(new VariableReturnStatement(v1, ret1))
                    .build(), translation);
    }

    @Test
    public void translateVarRelationshipNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), conf.mapNode(node22));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(v1, a1, a2))
                    .condition(new NodeIDCondition(a2, "22"))
                    .condition(new EdgeLabelCondition(a1, "DIRECTED"))
                    .returns(new VariableReturnStatement(v1, ret1))
                    .build(), translation);
    }

    @Test
    public void translateNodeLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.getLabel(), Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(v1))
                    .condition(new NodeIDCondition(v1, "22"))
                    .returns(new LabelsReturnStatement(v1, ret1))
                    .build(), translation);
    }

    @Test
    public void translateNodePropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.mapProperty("name"), Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(a1))
                    .condition(new NodeIDCondition(a1, "22"))
                    .condition(new PropertyEXISTSCondition(a1, "name"))
                    .returns(new PropertyValueReturnStatement(a1, "name", ret1))
                    .build(), translation);
    }

    @Test
    public void translateNodeRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(a1, a2, v1))
                    .condition(new NodeIDCondition(a1, "22"))
                    .condition(new EdgeLabelCondition(a2, "DIRECTED"))
                    .returns(new VariableReturnStatement(v1, ret1))
                    .build(), translation);
    }

    @Test
    public void translateNodeVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), conf.mapNode(node23));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(a1, v1, a2))
                    .condition(new NodeIDCondition(a1, "22"))
                    .condition(new NodeIDCondition(a2, "23"))
                    .returns(new VariableReturnStatement(v1, ret1))
                    .build(), translation);
    }

    @Test
    public void translateNodeVarLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple( conf.mapNode(node22), Var.alloc("p"),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(a1))
                    .condition(new NodeIDCondition(a1, "22"))
                    .returns(new FilteredPropertiesReturnStatement(a1, "Quentin Tarantino", ret1))
                    .build(), translation);
    }

    @Test
    public void translateNodeVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), conf.mapNodeLabel("Person"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(a1))
                        .condition(new NodeIDCondition(a1, node22.getId()))
                        .condition(new NodeLabelCondition(a1, "Person"))
                        .returns(new LiteralValueReturnStatement("label", ret1))
                        .build(), translation);
    }

    @Test
    public void translateVarLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.getLabel(), Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(v1))
                    .returns(new VariableReturnStatement(v1, ret1))
                    .returns(new LabelsReturnStatement(v1, ret2))
                    .build(), translation);
    }

    @Test
    public void translateVarRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(v1, a1, v2))
                    .condition(new EdgeLabelCondition(a1, "DIRECTED"))
                    .returns(new VariableReturnStatement(v1, ret1))
                    .returns(new VariableReturnStatement(v2, ret2))
                    .build(), translation);
    }

    @Test
    public void translateVarPropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapProperty("name"), Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                        CypherQueryBuilder.newBuilder()
                                .match(new EdgeMatchClause(src1, edge1, tgt1))
                                .condition(new PropertyEXISTSCondition(edge1, "name"))
                                .returns(new TripleMapReturnStatement(src1, edge1, tgt1, ret1))
                                .returns(new PropertyValueReturnStatement(edge1, "name", ret2))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(v1))
                                .condition(new PropertyEXISTSCondition(v1, "name"))
                                .returns(new VariableReturnStatement(v1, ret1))
                                .returns(new PropertyValueReturnStatement(v1, "name", ret2)).build()
                ), translation);
    }

    @Test
    public void translateVarVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), conf.mapNodeLabel("Person"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(v1))
                    .condition(new NodeLabelCondition(v1, "Person"))
                    .returns(new VariableReturnStatement(v1, ret1))
                    .build(), translation);
    }

    @Test
    public void translateVarVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), conf.mapNode(node22));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(v1, v2, a1))
                    .condition(new NodeIDCondition(a1, "22"))
                    .returns(new VariableReturnStatement(v1, ret1))
                    .returns(new VariableReturnStatement(v2, ret2))
                    .build(), translation);
    }

    @Test
    public void translateVarVarLiteral() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), NodeFactory.createLiteral("The Matrix"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                        CypherQueryBuilder.newBuilder()
                                .match(new EdgeMatchClause(src1, edge1, tgt1))
                                .returns(new TripleMapReturnStatement(src1, edge1, tgt1, ret1))
                                .returns(new FilteredPropertiesReturnStatement(edge1, "The Matrix", ret2))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(v1))
                                .returns(new VariableReturnStatement(v1, ret1))
                                .returns(new FilteredPropertiesReturnStatement(v1, "The Matrix", ret2))
                                .build()
                ), translation);
    }

    @Test
    public void translateNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(a1))
                                .condition(new NodeIDCondition(a1, "22"))
                                .returns(new LiteralValueReturnStatement("label", ret1))
                                .returns(new LabelsReturnStatement(a1, ret2))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(a2))
                                .condition(new NodeIDCondition(a2, "22"))
                                .returns(new PropertyListReturnStatement(a2, ret1))
                                .returns(new AllPropertyValuesReturnStatement(a2, ret2))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new EdgeMatchClause(a3, a4, a5))
                                .condition(new NodeIDCondition(a3, "22"))
                                .returns(new VariableReturnStatement(a4, ret1))
                                .returns(new VariableReturnStatement(a5, ret2))
                                .build()
                ), translation);
    }

    @Test
    public void translateVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = SPARQLStar2CypherTranslator.translateTriplePattern(new TriplePatternImpl(t), conf);
        assertEquals(
                new CypherUnionQueryImpl(
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(a1))
                                .returns(new VariableReturnStatement(a1, ret1))
                                .returns(new LiteralValueReturnStatement("label", ret2))
                                .returns(new LabelsReturnStatement(a1, ret3))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new NodeMatchClause(a2))
                                .returns(new VariableReturnStatement(a2, ret1))
                                .returns(new PropertyListReturnStatement(a2, ret2))
                                .returns(new AllPropertyValuesReturnStatement(a2, ret3))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new EdgeMatchClause(a3, a4, a5))
                                .returns(new TripleMapReturnStatement(a3, a4, a5, ret1))
                                .returns(new PropertyListReturnStatement(a4, ret2))
                                .returns(new AllPropertyValuesReturnStatement(a4, ret3))
                                .build(),
                        CypherQueryBuilder.newBuilder()
                                .match(new EdgeMatchClause(a6, a7, a8))
                                .returns(new VariableReturnStatement(a6, ret1))
                                .returns(new VariableReturnStatement(a7, ret2))
                                .returns(new VariableReturnStatement(a8, ret3))
                                .build()
                ), translation);
    }

}
