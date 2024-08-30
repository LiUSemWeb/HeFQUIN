package se.liu.ida.hefquin.engine.wrappers.lpg.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.junit.Before;
import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.lpg.SPARQLStar2CypherTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.AliasedExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.CountLargerThanZeroExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.EXISTSExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.EqualityExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.FirstLabelExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.GetItemExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.KeysExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.LiteralExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.MarkerExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.MembershipExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.PropertyAccessExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.PropertyAccessWithVarExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.TripleMapExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.TypeExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.UnwindIteratorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.VariableIDExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.VariableLabelExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match.PathMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherQueryBuilder;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherVarGenerator;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.LabeledGraph;

public class SPARQLStar2CypherTranslatorImplTest {

    CypherVarGenerator gen;

    final CypherVar ret1 = new CypherVar("ret1");
    final CypherVar ret2 = new CypherVar("ret2");
    final CypherVar ret3 = new CypherVar("ret3");

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
    final CypherVar marker = new CypherVar("m");

    final LPGNode node22 = new LPGNode("22", null, null);
    final LPGNode node23 = new LPGNode("23", null, null);

    final Set<Node> emptySet = Collections.emptySet();

    final LiteralExpression id22 = new LiteralExpression("22", XSDDatatype.XSDinteger);
    final LiteralExpression id23 = new LiteralExpression("23", XSDDatatype.XSDinteger);

    @Before
    public void resetVarGenerator() {
        gen = new CypherVarGenerator();
    }

    @Test
    public void translateNodeLabelLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple tp = Triple.create(conf.getRDFTermForLPGNode(node22), conf.getLabelPredicate(), conf.getRDFTermForNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(tp, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new VariableLabelExpression(a1, "Person"))
                        .add(new AliasedExpression(new CountLargerThanZeroExpression(), a2))
                        .build(),
                translation
        );
    }

    @Test
    public void translateNodePropertyLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple tp = Triple.create(conf.getRDFTermForLPGNode(node22), conf.getIRIForPropertyName("name"),
                NodeFactory.createLiteral("Uma Thurman"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(tp, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new EqualityExpression(new PropertyAccessExpression(a1, "name"),
                                new LiteralExpression("Uma Thurman")))
                        .add(new AliasedExpression(new CountLargerThanZeroExpression(), a2))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeRelationshipNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple tp = Triple.create(conf.getRDFTermForLPGNode(node22), conf.getIRIForEdgeLabel("directed"), conf.getRDFTermForLPGNode(node23));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(tp, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new VariableLabelExpression(a2, "directed"))
                        .add(new EqualityExpression(new VariableIDExpression(a3), id23))
                        .add(new AliasedExpression(new CountLargerThanZeroExpression(), a4))
                        .build(),
                translation);
    }

    @Test
    public void translateVarPropertyLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple tp = Triple.create(Var.alloc("s"), conf.getIRIForPropertyName("name"),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(tp, conf).getCypherQuery();
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a1))
                                .add(new EqualityExpression(new PropertyAccessExpression(a1, "name"),
                                        new LiteralExpression("Quentin Tarantino")))
                                .add(new MarkerExpression(0, marker))
                                .add(new AliasedExpression(a1, ret1))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a2, a3, a4))
                                .add(new EqualityExpression(new PropertyAccessExpression(a3, "name"),
                                        new LiteralExpression("Quentin Tarantino")))
                                .add(new MarkerExpression(1, marker))
                                .add(new AliasedExpression(new TripleMapExpression(a2, a3, a4), ret1))
                                .build())
                , translation);
    }

    @Test
    public void translateVarLabelClassTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(Var.alloc("s"), conf.getLabelPredicate(), conf.getRDFTermForNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new VariableLabelExpression(a1, "Person"))
                        .add(new AliasedExpression(a1, ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateVarRelationshipNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(Var.alloc("s"), conf.getIRIForEdgeLabel("DIRECTED"), conf.getRDFTermForLPGNode(node22));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new EqualityExpression(new VariableIDExpression(a3), id22))
                        .add(new VariableLabelExpression(a2, "DIRECTED"))
                        .add(new AliasedExpression(a1, ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), conf.getLabelPredicate(), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new AliasedExpression(new FirstLabelExpression(a1), ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodePropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), conf.getIRIForPropertyName("name"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new EXISTSExpression(new PropertyAccessExpression(a1, "name")))
                        .add(new AliasedExpression(new PropertyAccessExpression(a1, "name"), ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), conf.getIRIForEdgeLabel("DIRECTED"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new VariableLabelExpression(a2, "DIRECTED"))
                        .add(new AliasedExpression(a3, ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), Var.alloc("p"), conf.getRDFTermForLPGNode(node23));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new EqualityExpression(new VariableIDExpression(a3), id23))
                        .add(new AliasedExpression(new TypeExpression(a2), ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeVarLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), Var.alloc("p"),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a1),
                                List.of(new EqualityExpression(
                                        new PropertyAccessWithVarExpression(a1, vark),
                                        new LiteralExpression("Quentin Tarantino"))),
                                List.of(vark), a2))
                        .add(new AliasedExpression(new GetItemExpression(a2, 0), ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateNodeVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), Var.alloc("p"), conf.getRDFTermForNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new VariableLabelExpression(a1, "Person"))
                        .add(new AliasedExpression(new LiteralExpression("label"), ret1))
                        .build(),
                translation);
    }

    @Test
    public void translateVarLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(Var.alloc("s"), conf.getLabelPredicate(), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(new FirstLabelExpression(a1), ret2))
                        .build(),
                translation);
    }

    @Test
    public void translateVarRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(Var.alloc("s"), conf.getIRIForEdgeLabel("DIRECTED"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new VariableLabelExpression(a2, "DIRECTED"))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(a3, ret2))
                        .build(),
                translation);
    }

    @Test
    public void translateVarPropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(Var.alloc("s"), conf.getIRIForPropertyName("name"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a2, a3, a4))
                                .add(new EXISTSExpression(new PropertyAccessExpression(a3, "name")))
                                .add(new MarkerExpression(0, marker))
                                .add(new AliasedExpression(new TripleMapExpression(a2, a3, a4), ret1))
                                .add(new AliasedExpression(new PropertyAccessExpression(a3, "name"), ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a1))
                                .add(new EXISTSExpression(new PropertyAccessExpression(a1, "name")))
                                .add(new MarkerExpression(1, marker))
                                .add(new AliasedExpression(a1, ret1))
                                .add(new AliasedExpression(new PropertyAccessExpression(a1, "name"), ret2)).build()),
                translation);
    }

    @Test
    public void translateVarVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(Var.alloc("s"), Var.alloc("p"), conf.getRDFTermForNodeLabel("Person"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new VariableLabelExpression(a1, "Person"))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(new LiteralExpression("label"), ret2))
                        .build(),
                translation);
    }

    @Test
    public void translateVarVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(Var.alloc("s"), Var.alloc("p"), conf.getRDFTermForLPGNode(node22));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new EqualityExpression(new VariableIDExpression(a3), id22))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(new TypeExpression(a2), ret2))
                        .build(),
                translation);
    }

    @Test
    public void translateVarVarLiteral() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(Var.alloc("s"), Var.alloc("p"), NodeFactory.createLiteral("The Matrix"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a3, a4, a5))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a4),
                                        List.of(new EqualityExpression(
                                                new PropertyAccessWithVarExpression(a4, vark),
                                                new LiteralExpression("The Matrix"))),
                                        List.of(vark), a6))
                                .add(new MarkerExpression(0, marker))
                                .add(new AliasedExpression(new TripleMapExpression(a3, a4, a5), ret1))
                                .add(new AliasedExpression(new GetItemExpression(a6, 0), ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a1))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a1),
                                        List.of(new EqualityExpression(
                                                new PropertyAccessWithVarExpression(a1, vark),
                                                new LiteralExpression("The Matrix"))),
                                        List.of(vark), a2))
                                .add(new MarkerExpression(1, marker))
                                .add(new AliasedExpression(a1, ret1))
                                .add(new AliasedExpression(new GetItemExpression(a2, 0), ret2))
                                .build()),
                translation);
    }

    @Test
    public void translateNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a1))
                                .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                                .add(new MarkerExpression(0, marker))
                                .add(new AliasedExpression(new LiteralExpression("label"), ret1))
                                .add(new AliasedExpression(new FirstLabelExpression(a1), ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a2))
                                .add(new EqualityExpression(new VariableIDExpression(a2), id22))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a2), null,
                                        List.of(vark, new PropertyAccessWithVarExpression(a2, vark)), a3))
                                .add(new MarkerExpression(1, marker))
                                .add(new AliasedExpression(new GetItemExpression(a3, 0), ret1))
                                .add(new AliasedExpression(new GetItemExpression(a3, 1), ret2))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a4, a5, a6))
                                .add(new EqualityExpression(new VariableIDExpression(a4), id22))
                                .add(new MarkerExpression(2, marker))
                                .add(new AliasedExpression(new TypeExpression(a5), ret1))
                                .add(new AliasedExpression(a6, ret2))
                                .build()),
                translation);
    }

    @Test
    public void translateVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple t = Triple.create(Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a1, a2, a3))
                                .add(new MarkerExpression(0, marker))
                                .add(new AliasedExpression(a1, ret1))
                                .add(new AliasedExpression(new TypeExpression(a2), ret2))
                                .add(new AliasedExpression(a3, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(a4))
                                .add(new MarkerExpression(1, marker))
                                .add(new AliasedExpression(a4, ret1))
                                .add(new AliasedExpression(new LiteralExpression("label"), ret2))
                                .add(new AliasedExpression(new FirstLabelExpression(a4), ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a5))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a5), null,
                                        List.of(vark, new PropertyAccessWithVarExpression(a5, vark)), a6))
                                .add(new MarkerExpression(2, marker))
                                .add(new AliasedExpression(a5, ret1))
                                .add(new AliasedExpression(new GetItemExpression(a6, 0), ret2))
                                .add(new AliasedExpression(new GetItemExpression(a6, 1), ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a7, a8, a9))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a8), null,
                                        List.of(vark, new PropertyAccessWithVarExpression(a8, vark)), a10))
                                .add(new MarkerExpression(3, marker))
                                .add(new AliasedExpression(new TripleMapExpression(a7, a8, a9), ret1))
                                .add(new AliasedExpression(new GetItemExpression(a10, 0), ret2))
                                .add(new AliasedExpression(new GetItemExpression(a10, 1), ret3))
                                .build()),
                translation);
    }

    @Test
    public void certainNodeVarPropLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var s = Var.alloc("s");
        final Triple tp = Triple.create(s, conf.getIRIForPropertyName("name"),
                NodeFactory.createLiteral("Quentin Tarantino"));
        final Set<Node> certainNodes = Collections.singleton(s);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(tp, conf, gen, certainNodes, emptySet,
                        emptySet, emptySet, emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new PropertyAccessExpression(a1, "name"),
                                new LiteralExpression("Quentin Tarantino")))
                        .add(new AliasedExpression(a1, ret1))
                        .build()
                , translation);
    }

    @Test
    public void certainNodeVarVarLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var s = Var.alloc("s");
        final Triple t = Triple.create(s, Var.alloc("p"), NodeFactory.createLiteral("The Matrix"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, Collections.singleton(s),
                        emptySet, emptySet, emptySet, emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a1),
                                List.of(new EqualityExpression(
                                        new PropertyAccessWithVarExpression(a1, vark),
                                        new LiteralExpression("The Matrix"))),
                                List.of(vark), a2))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(new GetItemExpression(a2, 0), ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainNodeNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var o = Var.alloc("o");
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, Collections.singleton(o),
                        emptySet, emptySet, emptySet, emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, a2, a3))
                .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                .add(new AliasedExpression(new TypeExpression(a2), ret1))
                .add(new AliasedExpression(a3, ret2))
                .build(), translation);
    }

    @Test
    public void certainNodeVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var s = Var.alloc("s");
        final Triple t = Triple.create(s, Var.alloc("p"), Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, Collections.singleton(s),
                        emptySet, emptySet, emptySet, emptySet).getCypherQuery();
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a1, a2, a3))
                                .add(new MarkerExpression(0, marker))
                                .add(new AliasedExpression(a1, ret1))
                                .add(new AliasedExpression(new TypeExpression(a2), ret2))
                                .add(new AliasedExpression(a3, ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .addMatch(new NodeMatchClause(a4))
                                .add(new MarkerExpression(1, marker))
                                .add(new AliasedExpression(a4, ret1))
                                .add(new AliasedExpression(new LiteralExpression("label"), ret2))
                                .add(new AliasedExpression(new FirstLabelExpression(a4), ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a5))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a5), null,
                                        List.of(vark, new PropertyAccessWithVarExpression(a5, vark)), a6))
                                .add(new MarkerExpression(2, marker))
                                .add(new AliasedExpression(a5, ret1))
                                .add(new AliasedExpression(new GetItemExpression(a6, 0), ret2))
                                .add(new AliasedExpression(new GetItemExpression(a6, 1), ret3))
                                .build()),
                translation);
    }

    @Test
    public void certainNodeVarVarVarTest2() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var s = Var.alloc("s");
        final Var o = Var.alloc("o");
        final Triple t = Triple.create(s, Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, Set.of(s, o),
                        emptySet, emptySet, emptySet, emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(new TypeExpression(a2), ret2))
                        .add(new AliasedExpression(a3, ret3))
                        .build(),
                translation);
    }

    @Test
    public void certainLabelNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var o = Var.alloc("o");
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, emptySet, emptySet,
                        Collections.singleton(o), emptySet, emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new AliasedExpression(new LiteralExpression("label"), ret1))
                        .add(new AliasedExpression(new FirstLabelExpression(a1), ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainLabelVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var o = Var.alloc("o");
        final Triple t = Triple.create(Var.alloc("s"), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, emptySet, emptySet,
                        Collections.singleton(o), emptySet, emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .addMatch(new NodeMatchClause(a4))
                        .add(new AliasedExpression(a4, ret1))
                        .add(new AliasedExpression(new LiteralExpression("label"), ret2))
                        .add(new AliasedExpression(new FirstLabelExpression(a4), ret3))
                        .build(),
                translation);
    }

    @Test
    public void certainPropertyNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var p = Var.alloc("p");
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), p, Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, emptySet, emptySet, emptySet,
                        Collections.singleton(p), emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a1), null,
                                List.of(vark, new PropertyAccessWithVarExpression(a1, vark)), a2))
                        .add(new AliasedExpression(new GetItemExpression(a2, 0), ret1))
                        .add(new AliasedExpression(new GetItemExpression(a2, 1), ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainPropertyNodeVarVarTest2() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var o = Var.alloc("o");
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, emptySet, emptySet, emptySet,
                        emptySet, Collections.singleton(o)).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a1), null,
                                List.of(vark, new PropertyAccessWithVarExpression(a1, vark)), a2))
                        .add(new AliasedExpression(new GetItemExpression(a2, 0), ret1))
                        .add(new AliasedExpression(new GetItemExpression(a2, 1), ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainPropertyVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var p = Var.alloc("p");
        final Triple t = Triple.create(Var.alloc("s"), p, Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, emptySet, emptySet, emptySet,
                        Collections.singleton(p), emptySet).getCypherQuery();
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a1))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a1), null,
                                        List.of(vark, new PropertyAccessWithVarExpression(a1, vark)), a2))
                                .add(new MarkerExpression(0, marker))
                                .add(new AliasedExpression(a1, ret1))
                                .add(new AliasedExpression(new GetItemExpression(a2, 0), ret2))
                                .add(new AliasedExpression(new GetItemExpression(a2, 1), ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a3, a4, a5))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a4), null,
                                        List.of(vark, new PropertyAccessWithVarExpression(a4, vark)), a6))
                                .add(new MarkerExpression(1, marker))
                                .add(new AliasedExpression(new TripleMapExpression(a3, a4, a5), ret1))
                                .add(new AliasedExpression(new GetItemExpression(a6, 0), ret2))
                                .add(new AliasedExpression(new GetItemExpression(a6, 1), ret3))
                                .build()),
                translation);
    }

    @Test
    public void certainPropertyVarVarVarTest2() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var o = Var.alloc("o");
        final Triple t = Triple.create(Var.alloc("s"), Var.alloc("p"), o);
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, emptySet, emptySet, emptySet,
                        emptySet, Collections.singleton(o)).getCypherQuery();
        assertEquals(
                new CypherUnionQueryImpl(
                        new CypherQueryBuilder()
                                .add(new NodeMatchClause(a1))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a1), null,
                                        List.of(vark, new PropertyAccessWithVarExpression(a1, vark)), a2))
                                .add(new MarkerExpression(0, marker))
                                .add(new AliasedExpression(a1, ret1))
                                .add(new AliasedExpression(new GetItemExpression(a2, 0), ret2))
                                .add(new AliasedExpression(new GetItemExpression(a2, 1), ret3))
                                .build(),
                        new CypherQueryBuilder()
                                .add(new EdgeMatchClause(a3, a4, a5))
                                .add(new UnwindIteratorImpl(vark, new KeysExpression(a4), null,
                                        List.of(vark, new PropertyAccessWithVarExpression(a4, vark)), a6))
                                .add(new MarkerExpression(1, marker))
                                .add(new AliasedExpression(new TripleMapExpression(a3, a4, a5), ret1))
                                .add(new AliasedExpression(new GetItemExpression(a6, 0), ret2))
                                .add(new AliasedExpression(new GetItemExpression(a6, 1), ret3))
                                .build()),
                translation);
    }

    @Test
    public void certainEdgeNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var p = Var.alloc("p");
        final Triple t = Triple.create(conf.getRDFTermForLPGNode(node22), p, Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, emptySet,
                        Collections.singleton(p), emptySet, emptySet, emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id22))
                        .add(new AliasedExpression(new TypeExpression(a2), ret1))
                        .add(new AliasedExpression(a3, ret2))
                        .build(),
                translation);
    }

    @Test
    public void certainEdgeVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var p = Var.alloc("p");
        final Triple t = Triple.create(Var.alloc("s"), p, Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, emptySet,
                        Collections.singleton(p), emptySet, emptySet, emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(new TypeExpression(a2), ret2))
                        .add(new AliasedExpression(a3, ret3))
                        .build(),
                translation);
    }

    @Test
    public void certainNodeAndPropertyVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var s = Var.alloc("s");
        final Var p = Var.alloc("p");
        final Triple t = Triple.create(s, p, Var.alloc("o"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf, gen, Collections.singleton(s),
                        emptySet, emptySet, Collections.singleton(p), emptySet).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a1), List.of(),
                                List.of(vark, new PropertyAccessWithVarExpression(a1, vark)), a2))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(new GetItemExpression(a2, 0), ret2))
                        .add(new AliasedExpression(new GetItemExpression(a2, 1), ret3))
                        .build(),
                translation);
    }

    @Test
    public void translateTriplePropertyLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple inner = Triple.create(Var.alloc("s"), conf.getIRIForEdgeLabel("DIRECTED"), Var.alloc("o"));
        final Triple t = Triple.create(new Node_Triple(inner), conf.getIRIForPropertyName("certainty"),
                NodeFactory.createLiteral("0.8"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new VariableLabelExpression(a2, "DIRECTED"))
                        .add(new EqualityExpression(new PropertyAccessExpression(a2, "certainty"),
                                new LiteralExpression("0.8")))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(a3, ret2))
                .build(),
                translation);
    }

    @Test
    public void translateTriplePropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple inner = Triple.create(Var.alloc("s"), conf.getIRIForEdgeLabel("DIRECTED"), Var.alloc("o"));
        final Triple t = Triple.create(new Node_Triple(inner), conf.getIRIForPropertyName("certainty"), Var.alloc("c"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new VariableLabelExpression(a2, "DIRECTED"))
                        .add(new EXISTSExpression(new PropertyAccessExpression(a2, "certainty")))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(a3, ret2))
                        .add(new AliasedExpression(new PropertyAccessExpression(a2, "certainty"), ret3))
                        .build(),
                translation);
    }

    @Test
    public void translateTripleVarLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple inner = Triple.create(Var.alloc("s"), conf.getIRIForEdgeLabel("DIRECTED"), Var.alloc("o"));
        final Triple t = Triple.create(new Node_Triple(inner), Var.alloc("p"), NodeFactory.createLiteral("0.8"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new VariableLabelExpression(a2, "DIRECTED"))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a2),
                                List.of(new EqualityExpression(new PropertyAccessWithVarExpression(a2, vark),
                                        new LiteralExpression("0.8"))),
                                List.of(vark), a4))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(a3, ret2))
                        .add(new AliasedExpression(new GetItemExpression(a4, 0), ret3))
                        .build(),
                translation);
    }

    @Test
    public void translateTripleVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Triple inner = Triple.create(Var.alloc("s"), conf.getIRIForEdgeLabel("DIRECTED"), Var.alloc("o"));
        final Triple t = Triple.create(new Node_Triple(inner), Var.alloc("p"), Var.alloc("l"));
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl()
                .translateTriplePattern(t, conf).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new VariableLabelExpression(a2, "DIRECTED"))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a2), null,
                                List.of(vark, new PropertyAccessWithVarExpression(a2, vark)), a4))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(a3, ret2))
                        .add(new AliasedExpression(new GetItemExpression(a4, 0), ret3))
                        .add(new AliasedExpression(new GetItemExpression(a4, 1), new CypherVar("ret4")))
                        .build(),
                translation);
    }

    @Test
    public void translateBGPTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var m = Var.alloc("m");
        final Var p = Var.alloc("p");
        final Set<Triple> bgp = new HashSet<>();
        bgp.add( Triple.create(m, conf.getLabelPredicate(), conf.getRDFTermForNodeLabel("Movie")) );
        bgp.add( Triple.create(p, conf.getLabelPredicate(), conf.getRDFTermForNodeLabel("Person")) );
        bgp.add( Triple.create(p, conf.getIRIForPropertyName("name"), NodeFactory.createLiteral("Uma Thurman")) );
        bgp.add( Triple.create(m, conf.getIRIForPropertyName("released"), Var.alloc("y")) );
        bgp.add( Triple.create(NodeFactory.createTripleNode(p, conf.getIRIForEdgeLabel("ACTED_IN"), m),
                        conf.getIRIForPropertyName("source"), NodeFactory.createLiteral("IMDB")) );
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl().translateBGP(bgp, conf, false).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EdgeMatchClause(a2, a3, a4))
                        .add(new NodeMatchClause(a5))
                        .add(new NodeMatchClause(a6))
                        .add(new NodeMatchClause(a7))
                        .add(new EXISTSExpression(new PropertyAccessExpression(a1, "released")))
                        .add(new VariableLabelExpression(a3, "ACTED_IN"))
                        .add(new EqualityExpression(new PropertyAccessExpression(a3, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new EqualityExpression(a4, a1))
                        .add(new VariableLabelExpression(a5, "Movie"))
                        .add(new EqualityExpression(a5, a1))
                        .add(new EqualityExpression(new PropertyAccessExpression(a6, "name"),
                                new LiteralExpression("Uma Thurman")))
                        .add(new EqualityExpression(a6, a2))
                        .add(new VariableLabelExpression(a7, "Person"))
                        .add(new EqualityExpression(a7, a2))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(new PropertyAccessExpression(a1, "released"), ret2))
                        .add(new AliasedExpression(a2, ret3))
                        .build(),
                translation);
    }

    @Test
    public void translateBGPwithJoinRewriteTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var m = Var.alloc("m");
        final Var p = Var.alloc("p");
        final Set<Triple> bgp = new HashSet<>();
        bgp.add( Triple.create(m, conf.getLabelPredicate(), conf.getRDFTermForNodeLabel("Movie")) );
        bgp.add( Triple.create(p, conf.getLabelPredicate(), conf.getRDFTermForNodeLabel("Person")) );
        bgp.add( Triple.create(p, conf.getIRIForPropertyName("name"), NodeFactory.createLiteral("Uma Thurman")) );
        bgp.add( Triple.create(m, conf.getIRIForPropertyName("released"), Var.alloc("y")) );
        bgp.add( Triple.create(NodeFactory.createTripleNode(p, conf.getIRIForEdgeLabel("ACTED_IN"), m),
                        conf.getIRIForPropertyName("source"), NodeFactory.createLiteral("IMDB")) );

        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final CypherQuery procTranslation = translator.rewriteJoins((CypherMatchQuery) translator.translateBGP(bgp, conf, false).getCypherQuery());
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a2, a3, a4))
                        .add(new EXISTSExpression(new PropertyAccessExpression(a4, "released")))
                        .add(new VariableLabelExpression(a3, "ACTED_IN"))
                        .add(new EqualityExpression(new PropertyAccessExpression(a3, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new VariableLabelExpression(a4, "Movie"))
                        .add(new EqualityExpression(new PropertyAccessExpression(a2, "name"),
                                new LiteralExpression("Uma Thurman")))
                        .add(new VariableLabelExpression(a2, "Person"))
                        .add(new AliasedExpression(a4, ret1))
                        .add(new AliasedExpression(new PropertyAccessExpression(a4, "released"), ret2))
                        .add(new AliasedExpression(a2, ret3))
                        .build(),
                procTranslation);
    }

    @Test
    public void rewriteJoinsTest() {
        final CypherVar a11 = new CypherVar("a11");
        final CypherVar a12 = new CypherVar("a12");
        final CypherVar a13 = new CypherVar("a13");
        final CypherVar a14 = new CypherVar("a14");
        final CypherMatchQuery query = new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, a2, a3))
                .add(new EdgeMatchClause(a4, a5, a6))
                .add(new EdgeMatchClause(a7, a8, a9))
                .add(new EdgeMatchClause(a10, a11, a12))
                .add(new NodeMatchClause(a13))
                .add(new NodeMatchClause(a14))
                .add(new EqualityExpression(a1, a6))
                .add(new EqualityExpression(a14, a1))
                .add(new EqualityExpression(a4, a13))
                .add(new EqualityExpression(a7, a4))
                .add(new EqualityExpression(a13, a10))
                .add(new EqualityExpression(new PropertyAccessExpression(a13, "name"),
                        new LiteralExpression("Uma Thurman")))
                .add(new AliasedExpression(a13, ret1))
                .add(new AliasedExpression(a14, ret2))
                .add(new AliasedExpression(new TypeExpression(a5), ret3))
                .build();
        final CypherMatchQuery rewrittenQuery = new SPARQLStar2CypherTranslatorImpl().rewriteJoins(query);
        assertEquals(new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a14, a2, a3))
                        .add(new EdgeMatchClause(a4, a5, a14))
                        .add(new EdgeMatchClause(a4, a8, a9))
                        .add(new EdgeMatchClause(a4, a11, a12))
                        .add(new EqualityExpression(new PropertyAccessExpression(a4, "name"),
                                new LiteralExpression("Uma Thurman")))
                        .add(new AliasedExpression(a4, ret1))
                        .add(new AliasedExpression(a14, ret2))
                        .add(new AliasedExpression(new TypeExpression(a5), ret3))
                        .build(),
                rewrittenQuery);
    }

    @Test
    public void joinOnLiteralsTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var l = Var.alloc("l");
        final Set<Triple> bgp = new HashSet<>();
        bgp.add( Triple.create(conf.getRDFTermForLPGNode(node23), conf.getIRIForPropertyName("name"), l) );
        bgp.add( Triple.create(conf.getRDFTermForLPGNode(node22), conf.getIRIForPropertyName("name"), l) );

        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl().translateBGP(bgp, conf, false).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new NodeMatchClause(a2))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id23))
                        .add(new EXISTSExpression(new PropertyAccessExpression(a1, "name")))
                        .add(new EqualityExpression(new VariableIDExpression(a2), id22))
                        .add(new EXISTSExpression(new PropertyAccessExpression(a2, "name")))
                        .add(new EqualityExpression(new PropertyAccessExpression(a2, "name"),
                                new PropertyAccessExpression(a1, "name")))
                        .add(new AliasedExpression(new PropertyAccessExpression(a1, "name"), ret1))
                        .build(),
                translation);
    }

    @Test
    public void unionUnionCombineTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var s = Var.alloc("s");
        final Set<Triple> bgp = new HashSet<>();
        bgp.add( Triple.create(s, conf.getIRIForPropertyName("source"), NodeFactory.createLiteral("IMDB")) );
        bgp.add( Triple.create(s, Var.alloc("p"), Var.alloc("o")) );

        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl().translateBGP(bgp, conf, false).getCypherQuery();
        final CypherVar a11 = new CypherVar("a11");
        final CypherVar a12 = new CypherVar("a12");
        final CypherVar a13 = new CypherVar("a13");
        final CypherVar a14 = new CypherVar("a14");
        assertEquals(new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a5, a6, a7))
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new PropertyAccessExpression(a1, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new EqualityExpression(a1, a5))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(a5, ret1))
                        .add(new AliasedExpression(new TypeExpression(a6), ret2))
                        .add(new AliasedExpression(a7, ret3))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a8))
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new PropertyAccessExpression(a1, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new EqualityExpression(a1, a8))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(a8, ret1))
                        .add(new AliasedExpression(new LiteralExpression("label"), ret2))
                        .add(new AliasedExpression(new FirstLabelExpression(a8), ret3))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a9))
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new PropertyAccessExpression(a1, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new EqualityExpression(a1, a9))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a9), null,
                                List.of(vark, new PropertyAccessWithVarExpression(a9, vark)), a10))
                        .add(new MarkerExpression(2, marker))
                        .add(new AliasedExpression(a9, ret1))
                        .add(new AliasedExpression(new GetItemExpression(a10, 0), ret2))
                        .add(new AliasedExpression(new GetItemExpression(a10, 1), ret3))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a11, a12, a13))
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new PropertyAccessExpression(a1, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new EqualityExpression(a1, new TripleMapExpression(a11, a12, a13)))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a12), null,
                                List.of(vark, new PropertyAccessWithVarExpression(a12, vark)), a14))
                        .add(new MarkerExpression(3, marker))
                        .add(new AliasedExpression(new TripleMapExpression(a11, a12, a13), ret1))
                        .add(new AliasedExpression(new GetItemExpression(a14, 0), ret2))
                        .add(new AliasedExpression(new GetItemExpression(a14, 1), ret3))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a5, a6, a7))
                        .add(new EdgeMatchClause(a2, a3, a4))
                        .add(new EqualityExpression(new PropertyAccessExpression(a3, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new EqualityExpression(new TripleMapExpression(a2, a3, a4), a5))
                        .add(new MarkerExpression(4, marker))
                        .add(new AliasedExpression(a5, ret1))
                        .add(new AliasedExpression(new TypeExpression(a6), ret2))
                        .add(new AliasedExpression(a7, ret3))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a8))
                        .add(new EdgeMatchClause(a2, a3, a4))
                        .add(new EqualityExpression(new PropertyAccessExpression(a3, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new EqualityExpression(new TripleMapExpression(a2, a3, a4), a8))
                        .add(new MarkerExpression(5, marker))
                        .add(new AliasedExpression(a8, ret1))
                        .add(new AliasedExpression(new LiteralExpression("label"), ret2))
                        .add(new AliasedExpression(new FirstLabelExpression(a8), ret3))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a9))
                        .add(new EdgeMatchClause(a2, a3, a4))
                        .add(new EqualityExpression(new PropertyAccessExpression(a3, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new EqualityExpression(new TripleMapExpression(a2, a3, a4), a9))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a9), null,
                                List.of(vark, new PropertyAccessWithVarExpression(a9, vark)), a10))
                        .add(new MarkerExpression(6, marker))
                        .add(new AliasedExpression(a9, ret1))
                        .add(new AliasedExpression(new GetItemExpression(a10, 0), ret2))
                        .add(new AliasedExpression(new GetItemExpression(a10, 1), ret3))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a11, a12, a13))
                        .add(new EdgeMatchClause(a2, a3, a4))
                        .add(new EqualityExpression(new PropertyAccessExpression(a3, "source"),
                                new LiteralExpression("IMDB")))
                        .add(new EqualityExpression(new TripleMapExpression(a2, a3, a4),
                                new TripleMapExpression(a11, a12, a13)))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a12), null,
                                List.of(vark, new PropertyAccessWithVarExpression(a12, vark)), a14))
                        .add(new MarkerExpression(7, marker))
                        .add(new AliasedExpression(new TripleMapExpression(a11, a12, a13), ret1))
                        .add(new AliasedExpression(new GetItemExpression(a14, 0), ret2))
                        .add(new AliasedExpression(new GetItemExpression(a14, 1), ret3))
                        .build()
        ), translation);
    }

    @Test
    public void joinOnPredicateTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Var p = Var.alloc("p");
        final Set<Triple> bgp = new HashSet<>();
        bgp.add( Triple.create(conf.getRDFTermForLPGNode(node23), p, NodeFactory.createLiteral("2005")) );
        bgp.add( Triple.create(conf.getRDFTermForLPGNode(node22), p, Var.alloc("o")) );

        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl().translateBGP(bgp, conf, false).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new NodeMatchClause(a3))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id23))
                        .add(new EqualityExpression(new VariableIDExpression(a3), id22))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a1),
                                List.of(new EqualityExpression(
                                        new PropertyAccessWithVarExpression(a1, vark),
                                        new LiteralExpression("2005")),
                                        new MembershipExpression(vark, new KeysExpression(a3))),
                                List.of(vark, new PropertyAccessWithVarExpression(a3, vark)), a5))
                        .add(new AliasedExpression(new GetItemExpression(a5, 0), ret1))
                        .add(new AliasedExpression(new GetItemExpression(a5, 1), ret2))
                        .build(),
                translation);
    }

    @Test
    public void crossProductTest() {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Set<Triple> bgp = new HashSet<>();
        bgp.add( Triple.create(conf.getRDFTermForLPGNode(node23), Var.alloc("p1"), NodeFactory.createLiteral("2005")) );
        bgp.add( Triple.create(conf.getRDFTermForLPGNode(node22), Var.alloc("p2"), NodeFactory.createLiteral("2005")) );
        final CypherQuery translation = new SPARQLStar2CypherTranslatorImpl().translateBGP(bgp, conf, false).getCypherQuery();
        assertEquals(new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new NodeMatchClause(a3))
                        .add(new EqualityExpression(new VariableIDExpression(a1), id23))
                        .add(new EqualityExpression(new VariableIDExpression(a3), id22))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a1),
                                List.of(new EqualityExpression(new PropertyAccessWithVarExpression(a1, vark),
                                        new LiteralExpression("2005"))),
                                List.of(vark), a2))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a3),
                                List.of(new EqualityExpression(new PropertyAccessWithVarExpression(a3, vark),
                                        new LiteralExpression("2005"))),
                                List.of(vark), a4))
                        .add(new AliasedExpression(new GetItemExpression(a2, 0), ret1))
                        .add(new AliasedExpression(new GetItemExpression(a4, 0), ret2))
                        .build(),
                translation);
    }

    @Test
    public void mergePatternsTest() {
        final CypherVar a11 = new CypherVar("a11");
        final List<MatchClause> matchClauses = new ArrayList<>();
        matchClauses.add(new EdgeMatchClause(a1, a2, a3));
        matchClauses.add(new EdgeMatchClause(a3, a4, a5));
        matchClauses.add(new EdgeMatchClause(a7, a6, a5));
        matchClauses.add(new EdgeMatchClause(a7, a8, a9));
        matchClauses.add(new EdgeMatchClause(a7, a10, a11));
        final List<MatchClause> merged = new SPARQLStar2CypherTranslatorImpl().mergePaths(matchClauses);
        assertEquals(2, merged.size());
        assertEquals(new PathMatchClause(List.of(new PathMatchClause.EdgePattern(a11, a10, a7, LabeledGraph.Direction.RIGHT2LEFT),
                        new PathMatchClause.EdgePattern(a7, a6, a5, LabeledGraph.Direction.LEFT2RIGHT),
                        new PathMatchClause.EdgePattern(a5, a4, a3, LabeledGraph.Direction.RIGHT2LEFT),
                        new PathMatchClause.EdgePattern(a3, a2, a1, LabeledGraph.Direction.RIGHT2LEFT))),
                merged.get(0));
        assertEquals(new PathMatchClause(List.of(new PathMatchClause.EdgePattern(a7, a8, a9, LabeledGraph.Direction.LEFT2RIGHT))),
                merged.get(1));
    }

    @Test
    public void mergeStarPatternTest() {
        final List<MatchClause> matchClauses = new ArrayList<>();
        matchClauses.add(new EdgeMatchClause(a1, a2, a3));
        matchClauses.add(new EdgeMatchClause(a1, a4, a5));
        matchClauses.add(new EdgeMatchClause(a1, a6, a7));
        matchClauses.add(new EdgeMatchClause(a1, a8, a9));
        final List<MatchClause> merged = new SPARQLStar2CypherTranslatorImpl().mergePaths(matchClauses);
        assertEquals(2, merged.size());
        assertEquals(new PathMatchClause(List.of(new PathMatchClause.EdgePattern(a3, a2, a1, LabeledGraph.Direction.RIGHT2LEFT),
                new PathMatchClause.EdgePattern(a1, a8, a9, LabeledGraph.Direction.LEFT2RIGHT))), merged.get(0));
        assertEquals(new PathMatchClause(List.of(new PathMatchClause.EdgePattern(a5, a4, a1, LabeledGraph.Direction.RIGHT2LEFT),
                new PathMatchClause.EdgePattern(a1, a6, a7, LabeledGraph.Direction.LEFT2RIGHT))), merged.get(1));
    }

    @Test
    public void mergeCyclePatternTest() {
        final List<MatchClause> matchClauses = new ArrayList<>();
        matchClauses.add(new EdgeMatchClause(a1, a2, a3));
        matchClauses.add(new EdgeMatchClause(a3, a4, a5));
        matchClauses.add(new EdgeMatchClause(a5, a6, a7));
        matchClauses.add(new EdgeMatchClause(a7, a8, a1));
        final List<MatchClause> merged = new SPARQLStar2CypherTranslatorImpl().mergePaths(matchClauses);
        assertEquals(1, merged.size());
        assertEquals(new PathMatchClause(List.of(new PathMatchClause.EdgePattern(a3, a2, a1, LabeledGraph.Direction.RIGHT2LEFT),
                        new PathMatchClause.EdgePattern(a1, a8, a7, LabeledGraph.Direction.RIGHT2LEFT),
                        new PathMatchClause.EdgePattern(a7, a6, a5, LabeledGraph.Direction.RIGHT2LEFT),
                        new PathMatchClause.EdgePattern(a5, a4, a3, LabeledGraph.Direction.RIGHT2LEFT))),
                merged.get(0));
    }

    @Test
    public void mergeWithNodePatternsTest() {
        final List<MatchClause> matchClauses = new ArrayList<>();
        matchClauses.add(new EdgeMatchClause(a1, a2, a3));
        matchClauses.add(new EdgeMatchClause(a3, a4, a5));
        matchClauses.add(new EdgeMatchClause(a3, a6, a7));
        matchClauses.add(new NodeMatchClause(a8));
        final List<MatchClause> merged = new SPARQLStar2CypherTranslatorImpl().mergePaths(matchClauses);
        assertEquals(3, merged.size());
        assertEquals(new PathMatchClause(List.of(new PathMatchClause.EdgePattern(a5, a4, a3, LabeledGraph.Direction.RIGHT2LEFT),
                new PathMatchClause.EdgePattern(a3, a6, a7, LabeledGraph.Direction.LEFT2RIGHT))), merged.get(0));
        assertEquals(new PathMatchClause(List.of(new PathMatchClause.EdgePattern(a3, a2, a1, LabeledGraph.Direction.RIGHT2LEFT))),
                merged.get(1));
        assertEquals(new NodeMatchClause(a8), merged.get(2));
    }

    @Test
    public void mergeBranchPatternTest() {
        final CypherVar a11 = new CypherVar("a11");
        final CypherVar a12 = new CypherVar("a12");
        final CypherVar a13 = new CypherVar("a13");
        final List<MatchClause> matchClauses = new ArrayList<>();
        matchClauses.add(new EdgeMatchClause(a1, a2, a3));
        matchClauses.add(new EdgeMatchClause(a3, a4, a5));
        matchClauses.add(new EdgeMatchClause(a5, a6, a7));
        matchClauses.add(new EdgeMatchClause(a7, a8, a9));
        matchClauses.add(new EdgeMatchClause(a5, a10, a11));
        matchClauses.add(new EdgeMatchClause(a11, a12, a13));

        final List<MatchClause> merged = new SPARQLStar2CypherTranslatorImpl().mergePaths(matchClauses);
        assertEquals(2, merged.size());
        assertEquals(new PathMatchClause(List.of(new PathMatchClause.EdgePattern(a13, a12, a11, LabeledGraph.Direction.RIGHT2LEFT),
                        new PathMatchClause.EdgePattern(a11, a10, a5, LabeledGraph.Direction.RIGHT2LEFT),
                        new PathMatchClause.EdgePattern(a5, a6, a7, LabeledGraph.Direction.LEFT2RIGHT),
                        new PathMatchClause.EdgePattern(a7, a8, a9, LabeledGraph.Direction.LEFT2RIGHT))),
            merged.get(0));
        assertEquals(new PathMatchClause(List.of(new PathMatchClause.EdgePattern(a5, a4, a3, LabeledGraph.Direction.RIGHT2LEFT),
                new PathMatchClause.EdgePattern(a3, a2, a1, LabeledGraph.Direction.RIGHT2LEFT))),
                merged.get(1));
    }

}
