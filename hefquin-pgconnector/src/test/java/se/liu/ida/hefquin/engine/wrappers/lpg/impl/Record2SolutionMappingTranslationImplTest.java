package se.liu.ida.hefquin.engine.wrappers.lpg.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.liu.ida.hefquin.engine.wrappers.lpg.Neo4jException;
import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.AliasedExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.EXISTSExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.EqualityExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.FirstLabelExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.GetItemExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.KeysExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.LiteralExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.MarkerExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.PropertyAccessExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.PropertyAccessWithVarExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.TripleMapExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.TypeExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.UnwindIteratorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.VariableIDExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.VariableLabelExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherQueryBuilder;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherUtils;

/**
 * This tests are outdated, they need to be changed to include the latest changes
 *
 * ----
 * I am not sure what "the latest changes" are that the comment above refers to.
 * The comment itself was introduced in the following commit by Sebastian.
 *                                                                        -Olaf
 *
 * https://github.com/LiUSemWeb/HeFQUIN/commit/bdadb79d3d9edb8130b9cbb0ec90db692a7d9c4e
 *
 */
public class Record2SolutionMappingTranslationImplTest {

    final LPGNode node0 = new LPGNode("0", null, null);
    final LPGNode node1 = new LPGNode("1", null, null);
    final LPGNode node2 = new LPGNode("2", null, null);
    final LPGNode node3 = new LPGNode("3", null, null);
    final LPGNode node4 = new LPGNode("4", null, null);
    final LPGNode node5 = new LPGNode("5", null, null);
    final LPGNode node6 = new LPGNode("6", null, null);
    final LPGNode node7 = new LPGNode("7", null, null);
    final LPGNode node9 = new LPGNode("9", null, null);
    final LPGNode node10 = new LPGNode("10", null, null);
    final LPGNode node14 = new LPGNode("14", null, null);
    final LPGNode node87 = new LPGNode("87", null, null);

    final CypherVar cpvar1 = new CypherVar("cpvar1");
    final CypherVar cpvar2 = new CypherVar("cpvar2");
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

    final CypherVar vark = new CypherVar("k");
    final CypherVar marker = new CypherVar("m1");

    @Test
    public void translateVarPropertyLiteralTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        final CypherQuery query = new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(cpvar1))
                        .add(new EqualityExpression(new PropertyAccessExpression(cpvar1, "name"),
                                new LiteralExpression("Lana Wachowski")))
                        .add(new AliasedExpression(cpvar1, ret1))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(src1, edge1, tgt1))
                        .add(new EqualityExpression(new PropertyAccessExpression(edge1, "name"),
                                new LiteralExpression("Lana Wachowski")))
                        .add(new AliasedExpression(new TripleMapExpression(src1, edge1, tgt1), ret1))
                        .build());

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\",\"deleted\":false}]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);
        final BindingBuilder builder = Binding.builder();
        builder.add(Var.alloc("s"), conf.getRDFTermForLPGNode(node6));
        final Binding m1 = builder.build();
        assertEquals(1, solMaps.size());
        assertEquals(m1, solMaps.get(0));
    }

    @Test
    public void translateVarLabelClassTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new NodeMatchClause(cpvar1))
                .add(new VariableLabelExpression(cpvar1, "Person"))
                .add(new AliasedExpression(cpvar1, ret1))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"]," +
                "\"data\":[{\"row\":[{\"born\":1964,\"name\":\"Keanu\"}],\"meta\":[{\"id\":1,\"type\":\"node\"}]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Carrie-Anne\"}],\"meta\":[{\"id\":2,\"type\":\"node\"}]}," +
                "{\"row\":[{\"born\":1961,\"name\":\"Laurence\"}],\"meta\":[{\"id\":3,\"type\":\"node\"}]}," +
                "{\"row\":[{\"born\":1960,\"name\":\"Hugo\"}],\"meta\":[{\"id\":4,\"type\":\"node\"}]}," +
                "{\"row\":[{\"born\":1944,\"name\":\"Taylor\"}],\"meta\":[{\"id\":14,\"type\":\"node\"}]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node1)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node2)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node3)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node4)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node14)) );
        assertEquals(solMaps, expected);
    }

    @Test
    public void translateVarRelationshipNodeTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new EdgeMatchClause(cpvar1, a1, a2))
                .add(new EqualityExpression(new VariableIDExpression(a2), new LiteralExpression("9")))
                .add(new VariableLabelExpression(a1, "DIRECTED"))
                .add(new AliasedExpression(cpvar1, ret1))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}],\"meta\":[{\"id\":6,\"type\":\"node\"}]}" +
                ",{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"}],\"meta\":[{\"id\":5,\"type\":\"node\"}]}]}]" +
                ",\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node6)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node5)) );
        assertEquals(solMaps, expected);
    }

    @Test
    public void translateNodeLabelVarTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("o"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new NodeMatchClause(cpvar1))
                .add(new EqualityExpression(new VariableIDExpression(cpvar1),
                        new LiteralExpression("22")))
                .add(new AliasedExpression(new FirstLabelExpression(cpvar1), ret1))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[\"Person\"],\"meta\":[null]}]}]" +
                ",\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final Binding expected = BindingFactory.binding( Var.alloc("o"),
                                                         conf.getRDFTermForNodeLabel("Person") );
        assertEquals(1, solMaps.size());
        assertEquals(expected, solMaps.get(0));
    }

    @Test
    public void translateNodePropertyVarTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("o"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new EqualityExpression(new VariableIDExpression(a1), new LiteralExpression("22")))
                .add(new EXISTSExpression(new PropertyAccessExpression(a1, "name")))
                .add(new AliasedExpression(new PropertyAccessExpression(a1, "name"), ret1))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[\"Cuba Gooding Jr.\"],\"meta\":[null]}]}],\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final Binding expected = BindingFactory.binding( Var.alloc("o"),
                                                         NodeFactory.createLiteral("Cuba Gooding Jr.") );
        assertEquals(1, solMaps.size());
        assertEquals(expected, solMaps.get(0));
    }

    @Test
    public void translateNodeRelationshipVarTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("o"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, a2, cpvar1))
                .add(new EqualityExpression(new VariableIDExpression(a1), new LiteralExpression("5")))
                .add(new VariableLabelExpression(a2, "DIRECTED"))
                .add(new AliasedExpression(cpvar1, ret1))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[{\"title\":\"The Matrix Revolutions\",\"released\":2003}],\"meta\":[{\"id\":10,\"type\":\"node\"}]}," +
                "{\"row\":[{\"title\":\"The Matrix Reloaded\",\"released\":2003}],\"meta\":[{\"id\":9,\"type\":\"node\"}]}," +
                "{\"row\":[{\"title\":\"The Matrix\",\"released\":1999}],\"meta\":[{\"id\":0,\"type\":\"node\"}]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("o"), conf.getRDFTermForLPGNode(node10)) );
        expected.add( BindingFactory.binding(Var.alloc("o"), conf.getRDFTermForLPGNode(node9)) );
        expected.add( BindingFactory.binding(Var.alloc("o"), conf.getRDFTermForLPGNode(node0)) );
        assertEquals(expected, solMaps);
    }

    @Test
    public void translateNodeVarNodeTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("p"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, cpvar1, a2))
                .add(new EqualityExpression(new VariableIDExpression(a1), new LiteralExpression("5")))
                .add(new EqualityExpression(new VariableIDExpression(a2), new LiteralExpression("9")))
                .add(new AliasedExpression(new TypeExpression(cpvar1), ret1))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[\"DIRECTED\"],\"meta\":[null]}]}],\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final Binding expected = BindingFactory.binding( Var.alloc("p"),
                                                         conf.getIRIForEdgeLabel("DIRECTED") );
        assertEquals(1, solMaps.size());
        assertEquals(expected, solMaps.get(0));
    }

    @Test
    public void translateNodeVarLiteralTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("p"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new EqualityExpression(new VariableIDExpression(a1), new LiteralExpression("9")))
                .add(new UnwindIteratorImpl(vark, new KeysExpression(a1),
                        List.of(new EqualityExpression(new PropertyAccessWithVarExpression(a1, vark),
                                new LiteralExpression("Lana Washowski"))),
                        List.of(vark), a2))
                .add(new AliasedExpression(new GetItemExpression(a2, 0), ret1))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[\"name\"],\"meta\":[null]}]}],\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final Binding expected = BindingFactory.binding( Var.alloc("p"),
                                                         conf.getIRIForPropertyName("name") );
        assertEquals(1, solMaps.size());
        assertEquals(expected, solMaps.get(0));
    }

    @Test
    public void translateNodeVarLabelTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("p"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new EqualityExpression(new VariableIDExpression(a1), new LiteralExpression("5")))
                .add(new VariableLabelExpression(a1, "Person"))
                .add(new AliasedExpression(new LiteralExpression("label"), ret1))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[\"label\"],\"meta\":[null]}]}],\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final Binding expected = BindingFactory.binding(Var.alloc("p"), conf.getLabelPredicate());
        assertEquals(1, solMaps.size());
        assertEquals(expected, solMaps.get(0));
    }

    @Test
    public void translateVarLabelVarTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        varMap.put(ret2, Var.alloc("o"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new NodeMatchClause(cpvar1))
                .add(new AliasedExpression(cpvar1, ret1))
                .add(new AliasedExpression(new FirstLabelExpression(cpvar1), ret2))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"title\":\"The Matrix\",\"released\":1999},\"Movie\"],\"meta\":[{\"id\":0,\"type\":\"node\"},null]}," +
                "{\"row\":[{\"born\":1964,\"name\":\"Keanu\"},\"Person\"],\"meta\":[{\"id\":1,\"type\":\"node\"},null]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Carrie-Anne\"},\"Person\"],\"meta\":[{\"id\":2,\"type\":\"node\"},null]}," +
                "{\"row\":[{\"born\":1961,\"name\":\"Laurence\"},\"Person\"],\"meta\":[{\"id\":3,\"type\":\"node\"},null]}," +
                "{\"row\":[{\"born\":1960,\"name\":\"Hugo\"},\"Person\"],\"meta\":[{\"id\":4,\"type\":\"node\"},null]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node0),
                Var.alloc("o"), conf.getRDFTermForNodeLabel("Movie")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node1),
                Var.alloc("o"), conf.getRDFTermForNodeLabel("Person")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node2),
                Var.alloc("o"), conf.getRDFTermForNodeLabel("Person")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node3),
                Var.alloc("o"), conf.getRDFTermForNodeLabel("Person")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node4),
                Var.alloc("o"), conf.getRDFTermForNodeLabel("Person")) );
        assertEquals(expected, solMaps);
    }

    @Test
    public void translateVarRelationshipVarTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        varMap.put(ret2, Var.alloc("o"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new EdgeMatchClause(cpvar1, a1, cpvar2))
                .add(new VariableLabelExpression(a1, "DIRECTED"))
                .add(new AliasedExpression(cpvar1, ret1))
                .add(new AliasedExpression(cpvar2, ret2))
                .build();

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}," +
                    "{\"title\":\"The Matrix\",\"released\":1999}]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\"},{\"id\":0,\"type\":\"node\"}]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"}," +
                    "{\"title\":\"The Matrix\",\"released\":1999}]," +
                    "\"meta\":[{\"id\":5,\"type\":\"node\"},{\"id\":0,\"type\":\"node\"}]}," +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}," +
                    "{\"title\":\"The Matrix Reloaded\",\"released\":2003}]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\"},{\"id\":9,\"type\":\"node\"}]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"}," +
                    "{\"title\":\"The Matrix Reloaded\",\"released\":2003}]," +
                    "\"meta\":[{\"id\":5,\"type\":\"node\"},{\"id\":9,\"type\":\"node\"}]}," +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}," +
                    "{\"title\":\"The Matrix Revolutions\",\"released\":2003}]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\"},{\"id\":10,\"type\":\"node\"}]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node6),
                Var.alloc("o"), conf.getRDFTermForLPGNode(node0)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node5),
                Var.alloc("o"), conf.getRDFTermForLPGNode(node0)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node6),
                Var.alloc("o"), conf.getRDFTermForLPGNode(node9)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node5),
                Var.alloc("o"), conf.getRDFTermForLPGNode(node9)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node6),
                Var.alloc("o"), conf.getRDFTermForLPGNode(node10)) );
        assertEquals(expected, solMaps);
    }

    @Test
    public void translateVarPropertyVarTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        varMap.put(ret2, Var.alloc("o"));
        final CypherQuery query = new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(src1, edge1, tgt1))
                        .add(new EXISTSExpression(new PropertyAccessExpression(edge1, "name")))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(new TripleMapExpression(src1, edge1, tgt1), ret1))
                        .add(new AliasedExpression(new PropertyAccessExpression(edge1, "name"), ret2))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(cpvar1))
                        .add(new EXISTSExpression(new PropertyAccessExpression(cpvar1, "name")))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(cpvar1, ret1))
                        .add(new AliasedExpression(new PropertyAccessExpression(cpvar1, "name"), ret2))
                        .build()
        );

        final String neo4jResponse = "{\"results\":[{\"columns\":[\"m1\",\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[\"$1\",{\"born\":1964,\"name\":\"Keanu\"},\"Keanu\"],\"meta\":[null,{\"id\":1,\"type\":\"node\"},null]}," +
                "{\"row\":[\"$1\",{\"born\":1967,\"name\":\"Carrie-Anne\"},\"Carrie-Anne\"],\"meta\":[null,{\"id\":2,\"type\":\"node\"},null]}," +
                "{\"row\":[\"$1\",{\"born\":1961,\"name\":\"Laurence\"},\"Laurence\"],\"meta\":[null,{\"id\":3,\"type\":\"node\"},null]}," +
                "{\"row\":[\"$1\",{\"born\":1960,\"name\":\"Hugo\"},\"Hugo\"],\"meta\":[null,{\"id\":4,\"type\":\"node\"},null]}," +
                "{\"row\":[\"$1\",{\"born\":1967,\"name\":\"Andy\"},\"Andy\"],\"meta\":[null,{\"id\":5,\"type\":\"node\"},null]}]}]" +
                ",\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);

        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);

        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node1),
                Var.alloc("o"), NodeFactory.createLiteral("Keanu")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node2),
                Var.alloc("o"), NodeFactory.createLiteral("Carrie-Anne")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node3),
                Var.alloc("o"), NodeFactory.createLiteral("Laurence")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node4),
                Var.alloc("o"), NodeFactory.createLiteral("Hugo")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node5),
                Var.alloc("o"), NodeFactory.createLiteral("Andy")) );
        assertEquals(expected, solMaps);
    }

    @Test
    public void translateVarVarLabelTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        varMap.put(ret2, Var.alloc("p"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new NodeMatchClause(cpvar1))
                .add(new VariableLabelExpression(cpvar1, "Person"))
                .add(new AliasedExpression(cpvar1, ret1))
                .add(new AliasedExpression(new LiteralExpression("label"), ret2))
                .build();
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"born\":1964,\"name\":\"Keanu\"},\"label\"],\"meta\":[{\"id\":1,\"type\":\"node\"},null]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Carrie-Anne\"},\"label\"],\"meta\":[{\"id\":2,\"type\":\"node\"},null]}," +
                "{\"row\":[{\"born\":1961,\"name\":\"Laurence\"},\"label\"],\"meta\":[{\"id\":3,\"type\":\"node\"},null]}," +
                "{\"row\":[{\"born\":1960,\"name\":\"Hugo\"},\"label\"],\"meta\":[{\"id\":4,\"type\":\"node\"},null]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Andy\"},\"label\"],\"meta\":[{\"id\":5,\"type\":\"node\"},null]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);
        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);
        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node1),
                Var.alloc("p"), conf.getLabelPredicate()) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node2),
                Var.alloc("p"), conf.getLabelPredicate()) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node3),
                Var.alloc("p"), conf.getLabelPredicate()) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node4),
                Var.alloc("p"), conf.getLabelPredicate()) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node5),
                Var.alloc("p"), conf.getLabelPredicate()) );
        assertEquals(expected, solMaps);
    }

    @Test
    public void translateVarVarNodeTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        varMap.put(ret2, Var.alloc("p"));
        final CypherQuery query = new CypherQueryBuilder()
                .add(new EdgeMatchClause(cpvar1, cpvar2, a1))
                .add(new EqualityExpression(new VariableIDExpression(a1), new LiteralExpression("9")))
                .add(new AliasedExpression(cpvar1, ret1))
                .add(new AliasedExpression(new TypeExpression(cpvar2), ret2))
                .build();
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"born\":1952,\"name\":\"Joel Silver\"}, \"DIRECTED\"]," +
                    "\"meta\":[{\"id\":7,\"type\":\"node\"},null]}," +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}, \"DIRECTED\"]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\"}, null]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"}, \"DIRECTED\"]," +
                    "\"meta\":[{\"id\":5,\"type\":\"node\"}, null]}," +
                "{\"row\":[{\"born\":1960,\"name\":\"Hugo Weaving\"}, \"ACTED_IN\"]," +
                    "\"meta\":[{\"id\":4,\"type\":\"node\"}, null]}," +
                "{\"row\":[{\"born\":1961,\"name\":\"Laurence Fishburne\"}, \"ACTED_IN\"]," +
                    "\"meta\":[{\"id\":3,\"type\":\"node\"}, null]}]}]" +
                ",\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);
        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);
        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node7),
                Var.alloc("p"), conf.getIRIForEdgeLabel("DIRECTED")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node6),
                Var.alloc("p"), conf.getIRIForEdgeLabel("DIRECTED")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node5),
                Var.alloc("p"), conf.getIRIForEdgeLabel("DIRECTED")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node4),
                Var.alloc("p"), conf.getIRIForEdgeLabel("ACTED_IN")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node3),
                Var.alloc("p"), conf.getIRIForEdgeLabel("ACTED_IN")) );
        assertEquals(expected, solMaps);
    }

    @Test
    public void translateVarVarLiteral() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        varMap.put(ret2, Var.alloc("p"));
        final CypherQuery query = new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(src1, edge1, tgt1))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(edge1),
                                List.of(new EqualityExpression(new PropertyAccessWithVarExpression(edge1, vark),
                                        new LiteralExpression("The Matrix"))),
                                List.of(vark), a1))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(new TripleMapExpression(src1, edge1, tgt1), ret1))
                        .add(new AliasedExpression(new GetItemExpression(a1, 0), ret2))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(cpvar1))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(cpvar1),
                                List.of(new EqualityExpression(new PropertyAccessWithVarExpression(cpvar1, vark),
                                        new LiteralExpression("The Matrix"))),
                                List.of(vark), a2))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(cpvar1, ret1))
                        .add(new AliasedExpression(new GetItemExpression(cpvar1, 0), ret2))
                        .build()
        );
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"m1\",\"ret1\",\"ret2\"], \"data\":[{" +
                "\"row\":[\"$1\",{\"title\":\"The Matrix\",\"released\":1999},\"title\"]," +
                "\"meta\":[null,{\"id\":0,\"type\":\"node\",\"deleted\":false},null]}]}]" +
                ",\"errors\":[]}}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);
        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);
        final Binding expected = BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node0),
                Var.alloc("p"), conf.getIRIForPropertyName("title"));
        assertEquals(1, solMaps.size());
        assertEquals(expected, solMaps.get(0));
    }

    @Test
    public void translateNodeVarVarTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("p"));
        varMap.put(ret2, Var.alloc("o"));
        final CypherQuery query = new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1), new LiteralExpression("22")))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(new LiteralExpression("label"), ret1))
                        .add(new AliasedExpression(new FirstLabelExpression(a1), ret2))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a2))
                        .add(new EqualityExpression(new VariableIDExpression(a2), new LiteralExpression("22")))
                        .add(new UnwindIteratorImpl(vark, new KeysExpression(a2), null,
                                List.of(vark, new PropertyAccessWithVarExpression(a2, vark)), a3))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(new GetItemExpression(a3, 0), ret1))
                        .add(new AliasedExpression(new GetItemExpression(a3, 1), ret2))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a3, a4, a5))
                        .add(new EqualityExpression(new VariableIDExpression(a3), new LiteralExpression("22")))
                        .add(new MarkerExpression(2, marker))
                        .add(new AliasedExpression(new TypeExpression(a4), ret1))
                        .add(new AliasedExpression(a5, ret2))
                        .build()
        );
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"m1\",\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[\"$0\",\"label\",\"Person\"],\"meta\":[null,null, null]}," +
                "{\"row\":[\"$1\",\"born\",\"1986\"],\"meta\":[null,null,null]}," +
                "{\"row\":[\"$1\",\"name\",\"Cuba Gooding Jr.\"],\"meta\":[null,null,null]},"+
                "{\"row\":[\"$2\",\"ACTED_IN\",{\"title\":\"What Dreams May Come\",\"released\":1998}]," +
                    "\"meta\":[null,null,{\"id\":0,\"type\":\"node\"}]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);
        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);
        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("p"), conf.getLabelPredicate(),
                Var.alloc("o"), conf.getRDFTermForNodeLabel("Person")) );
        expected.add( BindingFactory.binding(Var.alloc("p"), conf.getIRIForPropertyName("born"),
                Var.alloc("o"), NodeFactory.createLiteral("1986")) );
        expected.add( BindingFactory.binding(Var.alloc("p"), conf.getIRIForPropertyName("name"),
                Var.alloc("o"), NodeFactory.createLiteral("Cuba Gooding Jr.")) );
        expected.add( BindingFactory.binding(Var.alloc("p"), conf.getIRIForEdgeLabel("ACTED_IN"),
                Var.alloc("o"), conf.getRDFTermForLPGNode(node0)) );
        assertEquals(expected, solMaps);
    }

    @Test
    public void translateVarVarVarTest() throws JsonProcessingException, Neo4jException {
        final LPG2RDFConfiguration conf = new DefaultLPG2RDFConfigurationForTests();
        final Map<CypherVar, Var> varMap = new HashMap<>();
        varMap.put(ret1, Var.alloc("s"));
        varMap.put(ret2, Var.alloc("p"));
        varMap.put(ret3, Var.alloc("o"));
        final CypherVar a9 = new CypherVar("a9");
        final CypherVar a10 = new CypherVar("a10");
        final CypherQuery query = new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(a1, ret1))
                        .add(new AliasedExpression(new TypeExpression(a2), ret2))
                        .add(new AliasedExpression(a3, ret3))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a4))
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
                        .build()
        );
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"m1\",\"ret1\",\"ret2\",\"ret3\"],\"data\":[" +
                "{\"row\":[\"$0\",{\"born\":1964,\"name\":\"Keanu Reeves\"},\"ACTED_IN\",{\"title\":\"The Replacements\",\"released\":2000}]," +
                "\"meta\":[null,{\"id\":1,\"type\":\"node\"},null,{\"id\":87,\"type\":\"node\"}]}," +
                "{\"row\":[\"$1\",{\"title\":\"The Matrix\",\"released\":1999},\"label\",\"Movie\"]," +
                "\"meta\":[null,{\"id\":0,\"type\":\"node\",\"deleted\":false},null,null]}," +
                "{\"row\":[\"$2\",{\"title\":\"The Matrix\",\"released\":1999},\"title\",\"The Matrix\"]," +
                "\"meta\":[null,{\"id\":0,\"type\":\"node\"},null,null]}," +
                "{\"row\":[\"$2\",{\"title\":\"The Matrix\",\"released\":1999},\"tagline\",\"Welcome to the Real World\"]," +
                "\"meta\":[null,{\"id\":0,\"type\":\"node\"},null,null]}," +
                "{\"row\":[\"$2\",{\"title\":\"The Matrix\",\"released\":1999},\"released\",1999]," +
                "\"meta\":[null,{\"id\":0,\"type\":\"node\"},null,null]}," +
                "{\"row\":[\"$3\",{\"e\":\"ACTED_IN\",\"s\":{\"born\":1964,\"name\":\"Keanu Reeves\"},\"t\":{\"title\":\"The Replacements\",\"released\":2000}},\"roles\",\"Shane Falco\"]," +
                "\"meta\":[null,null,{\"id\":1,\"type\":\"node\"},{\"id\":87,\"type\":\"node\"},null,null]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);
        final List<Binding> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, query, varMap);
        final List<Binding> expected = new ArrayList<>();
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node1),
                Var.alloc("p"), conf.getIRIForEdgeLabel("ACTED_IN"), Var.alloc("o"), conf.getRDFTermForLPGNode(node87)) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node0),
                Var.alloc("p"), conf.getLabelPredicate(), Var.alloc("o"), conf.getRDFTermForNodeLabel("Movie")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node0),
                Var.alloc("p"), conf.getIRIForPropertyName("title"), Var.alloc("o"), NodeFactory.createLiteral("The Matrix")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node0),
                Var.alloc("p"), conf.getIRIForPropertyName("tagline"), Var.alloc("o"), NodeFactory.createLiteral("Welcome to the Real World")) );
        expected.add( BindingFactory.binding(Var.alloc("s"), conf.getRDFTermForLPGNode(node0),
                Var.alloc("p"), conf.getIRIForPropertyName("released"), Var.alloc("o"), NodeFactory.createLiteral("1999")) );
        expected.add( BindingFactory.binding(Var.alloc("s"),
                NodeFactory.createTripleNode(conf.getRDFTermForLPGNode(node1), conf.getIRIForEdgeLabel("ACTED_IN"), conf.getRDFTermForLPGNode(node87)),
                Var.alloc("p"), conf.getIRIForPropertyName("roles"), Var.alloc("o"), NodeFactory.createLiteral("Shane Falco")) );
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), solMaps.get(i));
        }
    }
}
