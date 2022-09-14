package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.DefaultConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.SPARQLStar2CypherTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.UnwindIteratorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    final CypherVar a9 = new CypherVar("a9");
    final CypherVar a10 = new CypherVar("a10");

    final CypherVar vark = new CypherVar("k");

    final LPGNode node22 = new LPGNode("22", null, null);
    final LPGNode node23 = new LPGNode("23", null, null);

    final Set<Node> emptySet = Collections.emptySet();

    @Test
    public void translateNodeLabelLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple tp = new Triple(conf.mapNode(node22), conf.getLabel(), conf.mapNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(tp), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new NodeIDCondition(a1, "22"))
                        .add(new NodeLabelCondition(a1, "Person"))
                        .add(new CountLargerThanZeroReturnStatement(a2))
                        .build(),
                translation
        );
    }

    @Test
    public void translateNodePropertyLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple tp = new Triple(conf.mapNode(node22), conf.mapProperty("name"),
                NodeFactory.createLiteral("Uma Thurman"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(tp), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new NodeIDCondition(a1, "22"))
                        .add(new PropertyValueCondition(a1, "name", "Uma Thurman"))
                        .add(new CountLargerThanZeroReturnStatement(a2))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeRelationshipNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple tp = new Triple(conf.mapNode(node22), conf.mapEdgeLabel("directed"), conf.mapNode(node23));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(tp), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new NodeIDCondition(a1, "22"))
                        .add(new EdgeLabelCondition(a2, "directed"))
                        .add(new NodeIDCondition(a3, "23"))
                        .add(new CountLargerThanZeroReturnStatement(a4))
                        .build(),
                translation);
    }

    @Test
    public void translateVarPropertyLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple tp = new Triple(Var.alloc("s"), conf.mapProperty("name"),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(tp), conf).object1;
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(v1))
                                .add(new PropertyValueCondition(v1, "name", "Quentin Tarantino"))
                                .add(new VariableReturnStatement(v1, ret1))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(src1, edge1, tgt1))
                                .add(new PropertyValueCondition(edge1, "name", "Quentin Tarantino"))
                                .add(new TripleMapReturnStatement(src1, edge1, tgt1, ret1))
                                .build())
                , translation);
    }

    @Test
    public void translateVarLabelClassTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.getLabel(), conf.mapNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(v1))
                        .add(new NodeLabelCondition(v1, "Person"))
                        .add(new VariableReturnStatement(v1, ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateVarRelationshipNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), conf.mapNode(node22));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(v1, a1, a2))
                        .add(new NodeIDCondition(a2, "22"))
                        .add(new EdgeLabelCondition(a1, "DIRECTED"))
                        .add(new VariableReturnStatement(v1, ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.getLabel(), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(v1))
                        .add(new NodeIDCondition(v1, "22"))
                        .add(new LabelsReturnStatement(v1, ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodePropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.mapProperty("name"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new NodeIDCondition(a1, "22"))
                        .add(new PropertyEXISTSCondition(a1, "name"))
                        .add(new PropertyValueReturnStatement(a1, "name", ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, v1))
                        .add(new NodeIDCondition(a1, "22"))
                        .add(new EdgeLabelCondition(a2, "DIRECTED"))
                        .add(new VariableReturnStatement(v1, ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), conf.mapNode(node23));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, v1, a2))
                        .add(new NodeIDCondition(a1, "22"))
                        .add(new NodeIDCondition(a2, "23"))
                        .add(new RelationshipTypeReturnStatement(v1, ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeVarLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new NodeIDCondition(a1, "22"))
                        .add(new UnwindIteratorImpl(vark, "KEYS(" + a1 + ")",
                                List.of(new PropertyValueConditionWithVar(a1, vark, "Quentin Tarantino")),
                                List.of("k"), a2))
                        .add(new VariableGetItemReturnStatement(a2, 0, ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), conf.mapNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new NodeIDCondition(a1, node22.getId()))
                        .add(new NodeLabelCondition(a1, "Person"))
                        .add(new LiteralValueReturnStatement("label", ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateVarLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.getLabel(), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(v1))
                        .add(new VariableReturnStatement(v1, ret1))
                        .add(new LabelsReturnStatement(v1, ret2))
                        .build(),
                translation);
    }

    @Test
    public void translateVarRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(v1, a1, v2))
                        .add(new EdgeLabelCondition(a1, "DIRECTED"))
                        .add(new VariableReturnStatement(v1, ret1))
                        .add(new VariableReturnStatement(v2, ret2))
                        .build(),
                translation);
    }

    @Test
    public void translateVarPropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapProperty("name"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(src1, edge1, tgt1))
                                .add(new PropertyEXISTSCondition(edge1, "name"))
                                .add(new TripleMapReturnStatement(src1, edge1, tgt1, ret1))
                                .add(new PropertyValueReturnStatement(edge1, "name", ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(v1))
                                .add(new PropertyEXISTSCondition(v1, "name"))
                                .add(new VariableReturnStatement(v1, ret1))
                                .add(new PropertyValueReturnStatement(v1, "name", ret2)).build()),
                translation);
    }

    @Test
    public void translateVarVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), conf.mapNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(v1))
                        .add(new NodeLabelCondition(v1, "Person"))
                        .add(new VariableReturnStatement(v1, ret1))
                        .add(new LiteralValueReturnStatement("label", ret2))
                        .build(),
                translation);
    }

    @Test
    public void translateVarVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), conf.mapNode(node22));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(v1, v2, a1))
                        .add(new NodeIDCondition(a1, "22"))
                        .add(new VariableReturnStatement(v1, ret1))
                        .add(new RelationshipTypeReturnStatement(v2, ret2))
                        .build(),
                translation);
    }

    @Test
    public void translateVarVarLiteral() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), NodeFactory.createLiteral("The Matrix"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(src1, edge1, tgt1))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + edge1 + ")",
                                        List.of(new PropertyValueConditionWithVar(edge1, vark, "The Matrix")),
                                        List.of("k"), a2))
                                .add(new TripleMapReturnStatement(src1, edge1, tgt1, ret1))
                                .add(new VariableGetItemReturnStatement(a2, 0, ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(v1))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + v1 + ")",
                                        List.of(new PropertyValueConditionWithVar(v1, vark, "The Matrix")),
                                        List.of("k"), a1))
                                .add(new VariableReturnStatement(v1, ret1))
                                .add(new VariableGetItemReturnStatement(a1, 0, ret2))
                                .build()),
                translation);
    }

    @Test
    public void translateNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a1))
                                .add(new NodeIDCondition(a1, "22"))
                                .add(new LiteralValueReturnStatement("label", ret1))
                                .add(new LabelsReturnStatement(a1, ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a2))
                                .add(new NodeIDCondition(a2, "22"))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + a2 + ")", null,
                                        List.of("k", a2 + "[k]"), a3))
                                .add(new VariableGetItemReturnStatement(a3, 0, ret1))
                                .add(new VariableGetItemReturnStatement(a3, 1, ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a4, a5, a6))
                                .add(new NodeIDCondition(a4, "22"))
                                .add(new RelationshipTypeReturnStatement(a5, ret1))
                                .add(new VariableReturnStatement(a6, ret2))
                                .build()),
                translation);
    }

    @Test
    public void translateVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a1, a2, a3))
                                .add(new VariableReturnStatement(a1, ret1))
                                .add(new RelationshipTypeReturnStatement(a2, ret2))
                                .add(new VariableReturnStatement(a3, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(a4))
                                .add(new VariableReturnStatement(a4, ret1))
                                .add(new LiteralValueReturnStatement("label", ret2))
                                .add(new LabelsReturnStatement(a4, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a5))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + a5 + ")",
                                        null, List.of("k", a5 + "[k]"), a6))
                                .add(new VariableReturnStatement(a5, ret1))
                                .add(new VariableGetItemReturnStatement(a6, 0, ret2))
                                .add(new VariableGetItemReturnStatement(a6, 1, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a7, a8, a9))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + a8 + ")",
                                        null, List.of("k", a8 + "[k]"), a10))
                                .add(new TripleMapReturnStatement(a7, a8, a9, ret1))
                                .add(new VariableGetItemReturnStatement(a10, 0, ret2))
                                .add(new VariableGetItemReturnStatement(a10, 1, ret3))
                                .build()),
                translation);
    }

    @Test
    public void certainNodeVarPropLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var s = Var.alloc("s");
        final Triple tp = new Triple(s, conf.mapProperty("name"),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final Set<Node> certainNodes = Collections.singleton(s);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(tp), conf, certainNodes, emptySet,
                        emptySet, emptySet, emptySet).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(v1))
                        .add(new PropertyValueCondition(v1, "name", "Quentin Tarantino"))
                        .add(new VariableReturnStatement(v1, ret1))
                        .build()
                , translation);
    }

    @Test
    public void certainNodeVarVarLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var s = Var.alloc("s");
        final Triple t = new Triple(s, Var.alloc("p"), NodeFactory.createLiteral("The Matrix"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, Collections.singleton(s),
                        emptySet, emptySet, emptySet, emptySet).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(v1))
                        .add(new UnwindIteratorImpl(vark, "KEYS(" + v1 + ")",
                                List.of(new PropertyValueConditionWithVar(v1, vark, "The Matrix")),
                                List.of("k"), a1))
                        .add(new VariableReturnStatement(v1, ret1))
                        .add(new VariableGetItemReturnStatement(a1, 0, ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainNodeNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var o = Var.alloc("o");
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, Collections.singleton(o),
                        emptySet, emptySet, emptySet, emptySet).object1;
        assertEquals(new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, v1, v2))
                .add(new NodeIDCondition(a1, "22"))
                .add(new RelationshipTypeReturnStatement(v1, ret1))
                .add(new VariableReturnStatement(v2, ret2))
                .build(), translation);
    }

    @Test
    public void certainNodeVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var s = Var.alloc("s");
        final Triple t = new Triple(s, Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, Collections.singleton(s),
                        emptySet, emptySet, emptySet, emptySet).object1;
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a1, a2, a3))
                                .add(new VariableReturnStatement(a1, ret1))
                                .add(new RelationshipTypeReturnStatement(a2, ret2))
                                .add(new VariableReturnStatement(a3, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(a4))
                                .add(new VariableReturnStatement(a4, ret1))
                                .add(new LiteralValueReturnStatement("label", ret2))
                                .add(new LabelsReturnStatement(a4, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a5))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + a5 + ")",
                                        null, List.of("k", a5 + "[k]"), a6))
                                .add(new VariableReturnStatement(a5, ret1))
                                .add(new VariableGetItemReturnStatement(a6, 0, ret2))
                                .add(new VariableGetItemReturnStatement(a6, 1, ret3))
                                .build()),
                translation);
    }

    @Test
    public void certainNodeVarVarVarTest2() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var s = Var.alloc("s");
        final Var o = Var.alloc("o");
        final Triple t = new Triple(s, Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, Set.of(s, o),
                        emptySet, emptySet, emptySet, emptySet).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new VariableReturnStatement(a1, ret1))
                        .add(new RelationshipTypeReturnStatement(a2, ret2))
                        .add(new VariableReturnStatement(a3, ret3))
                        .build(),
                translation);
    }

    @Test
    public void certainLabelNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var o = Var.alloc("o");
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, emptySet, emptySet,
                        Collections.singleton(o), emptySet, emptySet).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new NodeIDCondition(a1, "22"))
                        .add(new LiteralValueReturnStatement("label", ret1))
                        .add(new LabelsReturnStatement(a1, ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainLabelVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var o = Var.alloc("o");
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, emptySet, emptySet,
                        Collections.singleton(o), emptySet, emptySet).object1;
        assertEquals(new CypherQueryBuilder()
                        .addMatch(new NodeMatchClause(a4))
                        .add(new VariableReturnStatement(a4, ret1))
                        .add(new LiteralValueReturnStatement("label", ret2))
                        .add(new LabelsReturnStatement(a4, ret3))
                        .build(),
                translation);
    }

    @Test
    public void certainPropertyNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var p = Var.alloc("p");
        final Triple t = new Triple(conf.mapNode(node22), p, Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, emptySet, emptySet, emptySet,
                        Collections.singleton(p), emptySet).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a2))
                        .add(new NodeIDCondition(a2, "22"))
                        .add(new UnwindIteratorImpl(vark, "KEYS(" + a2 + ")", null,
                                List.of("k", a2 + "[k]"), a3))
                        .add(new VariableGetItemReturnStatement(a3, 0, ret1))
                        .add(new VariableGetItemReturnStatement(a3, 1, ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainPropertyNodeVarVarTest2() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var o = Var.alloc("o");
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, emptySet, emptySet, emptySet,
                        emptySet, Collections.singleton(o)).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a2))
                        .add(new NodeIDCondition(a2, "22"))
                        .add(new UnwindIteratorImpl(vark, "KEYS(" + a2 + ")", null,
                                List.of("k", a2 + "[k]"), a3))
                        .add(new VariableGetItemReturnStatement(a3, 0, ret1))
                        .add(new VariableGetItemReturnStatement(a3, 1, ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainPropertyVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var p = Var.alloc("p");
        final Triple t = new Triple(Var.alloc("s"), p, Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, emptySet, emptySet, emptySet,
                        Collections.singleton(p), emptySet).object1;
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a5))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + a5 + ")",
                                        null, List.of("k", a5 + "[k]"), a6))
                                .add(new VariableReturnStatement(a5, ret1))
                                .add(new VariableGetItemReturnStatement(a6, 0, ret2))
                                .add(new VariableGetItemReturnStatement(a6, 1, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a7, a8, a9))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + a8 + ")",
                                        null, List.of("k", a8 + "[k]"), a10))
                                .add(new TripleMapReturnStatement(a7, a8, a9, ret1))
                                .add(new VariableGetItemReturnStatement(a10, 0, ret2))
                                .add(new VariableGetItemReturnStatement(a10, 1, ret3))
                                .build()),
                translation);
    }

    @Test
    public void certainPropertyVarVarVarTest2() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var o = Var.alloc("o");
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, emptySet, emptySet, emptySet,
                        emptySet, Collections.singleton(o)).object1;
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a5))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + a5 + ")",
                                        null, List.of("k", a5 + "[k]"), a6))
                                .add(new VariableReturnStatement(a5, ret1))
                                .add(new VariableGetItemReturnStatement(a6, 0, ret2))
                                .add(new VariableGetItemReturnStatement(a6, 1, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a7, a8, a9))
                                .add(new UnwindIteratorImpl(vark, "KEYS(" + a8 + ")",
                                        null, List.of("k", a8 + "[k]"), a10))
                                .add(new TripleMapReturnStatement(a7, a8, a9, ret1))
                                .add(new VariableGetItemReturnStatement(a10, 0, ret2))
                                .add(new VariableGetItemReturnStatement(a10, 1, ret3))
                                .build()),
                translation);
    }

    @Test
    public void certainEdgeNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var p = Var.alloc("p");
        final Triple t = new Triple(conf.mapNode(node22), p, Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, emptySet,
                        Collections.singleton(p), emptySet, emptySet, emptySet).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a4, a5, a6))
                        .add(new NodeIDCondition(a4, "22"))
                        .add(new RelationshipTypeReturnStatement(a5, ret1))
                        .add(new VariableReturnStatement(a6, ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainEdgeVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Var p = Var.alloc("p");
        final Triple t = new Triple(Var.alloc("s"), p, Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf, emptySet,
                        Collections.singleton(p), emptySet, emptySet, emptySet).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new VariableReturnStatement(a1, ret1))
                        .add(new RelationshipTypeReturnStatement(a2, ret2))
                        .add(new VariableReturnStatement(a3, ret3))
                        .build(),
                translation);
    }

    @Test
    public void translateTriplePropertyLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple inner = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final Triple t = new Triple(new Node_Triple(inner), conf.mapProperty("certainty"),
                NodeFactory.createLiteral("0.8"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(v1, a1, v2))
                        .add(new EdgeLabelCondition(a1, "DIRECTED"))
                        .add(new PropertyValueCondition(a1, "certainty", "0.8"))
                        .add(new VariableReturnStatement(v1, ret1))
                        .add(new VariableReturnStatement(v2, ret2))
                .build(),
                translation);
    }

    @Test
    public void translateTriplePropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple inner = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final Triple t = new Triple(new Node_Triple(inner), conf.mapProperty("certainty"), Var.alloc("c"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(v1, a1, v2))
                        .add(new EdgeLabelCondition(a1, "DIRECTED"))
                        .add(new PropertyEXISTSCondition(a1, "certainty"))
                        .add(new VariableReturnStatement(v1, ret1))
                        .add(new VariableReturnStatement(v2, ret2))
                        .add(new PropertyValueReturnStatement(a1, "certainty", ret3))
                        .build(),
                translation);
    }

    @Test
    public void translateTripleVarLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple inner = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final Triple t = new Triple(new Node_Triple(inner), Var.alloc("p"), NodeFactory.createLiteral("0.8"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(v1, a1, v2))
                        .add(new EdgeLabelCondition(a1, "DIRECTED"))
                        .add(new UnwindIteratorImpl(vark, "KEYS("+a1+")",
                                List.of(new PropertyValueConditionWithVar(a1, vark, "0.8")),
                                List.of("k"), a2))
                        .add(new VariableReturnStatement(v1, ret1))
                        .add(new VariableReturnStatement(v2, ret2))
                        .add(new VariableGetItemReturnStatement(a2, 0, ret3))
                        .build(),
                translation);
    }

    @Test
    public void translateTripleVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple inner = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final Triple t = new Triple(new Node_Triple(inner), Var.alloc("p"), Var.alloc("l"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(new TriplePatternImpl(t), conf).object1;
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(v1, a1, v2))
                        .add(new EdgeLabelCondition(a1, "DIRECTED"))
                        .add(new UnwindIteratorImpl(vark, "KEYS("+a1+")", null,
                                List.of("k", a1+"[k]"), a2))
                        .add(new VariableReturnStatement(v1, ret1))
                        .add(new VariableReturnStatement(v2, ret2))
                        .add(new VariableGetItemReturnStatement(a2, 0, ret3))
                        .add(new VariableGetItemReturnStatement(a2, 1, new CypherVar("ret4")))
                        .build(),
                translation);
    }

}
