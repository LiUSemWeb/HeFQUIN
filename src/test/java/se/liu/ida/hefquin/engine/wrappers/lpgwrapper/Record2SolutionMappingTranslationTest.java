package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.federation.access.Neo4JException;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.DefaultConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.Record2SolutionMappingTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.SPARQLStar2CypherTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Record2SolutionMappingTranslationTest {

    final LPGNode node1 = new LPGNode("1", null, null);
    final LPGNode node2 = new LPGNode("2", null, null);
    final LPGNode node3 = new LPGNode("3", null, null);
    final LPGNode node4 = new LPGNode("4", null, null);
    final LPGNode node5 = new LPGNode("5", null, null);
    final LPGNode node6 = new LPGNode("6", null, null);
    final LPGNode node9 = new LPGNode("9", null, null);
    final LPGNode node14 = new LPGNode("14", null, null);
    final LPGNode node22 = new LPGNode("22", null, null);

    @Test
    public void translateVarPropertyLiteralTest() throws JsonProcessingException, Neo4JException {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapProperty("name"),
                NodeFactory.createLiteral("Lana Wachowski"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\",\"deleted\":false}]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);
        final List<SolutionMapping> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, translation.object1, translation.object2);
        final BindingBuilder builder = Binding.builder();
        builder.add(Var.alloc("s"), conf.mapNode(node6));
        final SolutionMapping m1 = new SolutionMappingImpl(builder.build());
        assertEquals(1, solMaps.size());
        assertEquals(m1, solMaps.get(0));
    }

    @Test
    public void translateVarLabelClassTest() throws JsonProcessingException, Neo4JException {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.getLabel(), conf.mapNodeLabel("Person"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"]," +
                "\"data\":[{\"row\":[{\"born\":1964,\"name\":\"Keanu Reeves\"}],\"meta\":[{\"id\":1,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Carrie-Anne Moss\"}],\"meta\":[{\"id\":2,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1961,\"name\":\"Laurence Fishburne\"}],\"meta\":[{\"id\":3,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1960,\"name\":\"Hugo Weaving\"}],\"meta\":[{\"id\":4,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1944,\"name\":\"Taylor Hackford\"}],\"meta\":[{\"id\":14,\"type\":\"node\",\"deleted\":false}]}]}]," +
                "\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);
        final List<SolutionMapping> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, translation.object1, translation.object2);
        List<SolutionMapping> expected = new ArrayList<>();
        expected.add(new SolutionMappingImpl(BindingFactory.binding(Var.alloc("s"), conf.mapNode(node1))));
        expected.add(new SolutionMappingImpl(BindingFactory.binding(Var.alloc("s"), conf.mapNode(node2))));
        expected.add(new SolutionMappingImpl(BindingFactory.binding(Var.alloc("s"), conf.mapNode(node3))));
        expected.add(new SolutionMappingImpl(BindingFactory.binding(Var.alloc("s"), conf.mapNode(node4))));
        expected.add(new SolutionMappingImpl(BindingFactory.binding(Var.alloc("s"), conf.mapNode(node14))));
        assertEquals(solMaps, expected);
    }

    @Test
    public void translateVarRelationshipNodeTest() throws JsonProcessingException, Neo4JException {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), conf.mapNode(node9));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}],\"meta\":[{\"id\":6,\"type\":\"node\",\"deleted\":false}]}" +
                ",{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"}],\"meta\":[{\"id\":5,\"type\":\"node\",\"deleted\":false}]}]}]" +
                ",\"errors\":[]}";
        final List<TableRecord> records = CypherUtils.parse(neo4jResponse);
        final List<SolutionMapping> solMaps = new Record2SolutionMappingTranslatorImpl()
                .translateRecords(records, conf, translation.object1, translation.object2);
        List<SolutionMapping> expected = new ArrayList<>();
        expected.add(new SolutionMappingImpl(BindingFactory.binding(Var.alloc("s"), conf.mapNode(node6))));
        expected.add(new SolutionMappingImpl(BindingFactory.binding(Var.alloc("s"), conf.mapNode(node5))));
        assertEquals(solMaps, expected);
    }

    @Test
    public void translateNodeLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.getLabel(), Var.alloc("o"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[\"Person\"],\"meta\":[null]}]}]" +
                ",\"errors\":[]}";
    }

    @Test
    public void translateNodePropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), conf.mapProperty("name"), Var.alloc("o"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[\"Cuba Gooding Jr.\"],\"meta\":[null]}]}],\"errors\":[]}";
    }

    @Test
    public void translateNodeRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node5), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[{\"tagline\":\"Speed has no limits\",\"title\":\"Speed Racer\",\"released\":2008}],\"meta\":[{\"id\":119,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"tagline\":\"Everything is connected\",\"title\":\"Cloud Atlas\",\"released\":2012}],\"meta\":[{\"id\":105,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"tagline\":\"Everything that has a beginning has an end\",\"title\":\"The Matrix Revolutions\",\"released\":2003}],\"meta\":[{\"id\":10,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"tagline\":\"Free your mind\",\"title\":\"The Matrix Reloaded\",\"released\":2003}],\"meta\":[{\"id\":9,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"tagline\":\"Welcome to the Real World\",\"title\":\"The Matrix\",\"released\":1999}],\"meta\":[{\"id\":0,\"type\":\"node\",\"deleted\":false}]}]}]," +
                "\"errors\":[]}";
    }

    @Test
    public void translateNodeVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node5), Var.alloc("p"), conf.mapNode(node9));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[{}],\"meta\":[{\"id\":12,\"type\":\"relationship\",\"deleted\":false}]}]}],\"errors\":[]}";
    }

    @Test
    public void translateNodeVarLiteralTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple( conf.mapNode(node9), Var.alloc("p"),
                NodeFactory.createLiteral("Lana Wachowski"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[[\"name\"]],\"meta\":[null]}]}],\"errors\":[]}";
    }

    @Test
    public void translateNodeVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node5), Var.alloc("p"), conf.mapNodeLabel("Person"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\"],\"data\":[" +
                "{\"row\":[\"label\"],\"meta\":[null]}]}],\"errors\":[]}";
        System.out.println(translation);
    }

    @Test
    public void translateVarLabelVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.getLabel(), Var.alloc("o"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"tagline\":\"Welcome to the Real World\",\"title\":\"The Matrix\",\"released\":1999},\"Movie\"],\"meta\":[{\"id\":0,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1964,\"name\":\"Keanu Reeves\"},\"Person\"],\"meta\":[{\"id\":1,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Carrie-Anne Moss\"},\"Person\"],\"meta\":[{\"id\":2,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1961,\"name\":\"Laurence Fishburne\"},\"Person\"],\"meta\":[{\"id\":3,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1960,\"name\":\"Hugo Weaving\"},\"Person\"],\"meta\":[{\"id\":4,\"type\":\"node\",\"deleted\":false},null]}]}]," +
                "\"errors\":[]}";
    }

    @Test
    public void translateVarRelationshipVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapEdgeLabel("DIRECTED"), Var.alloc("o"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}," +
                    "{\"tagline\":\"Welcome to the Real World\",\"title\":\"The Matrix\",\"released\":1999}]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\",\"deleted\":false},{\"id\":0,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"}," +
                    "{\"tagline\":\"Welcome to the Real World\",\"title\":\"The Matrix\",\"released\":1999}]," +
                    "\"meta\":[{\"id\":5,\"type\":\"node\",\"deleted\":false},{\"id\":0,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}," +
                    "{\"tagline\":\"Free your mind\",\"title\":\"The Matrix Reloaded\",\"released\":2003}]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\",\"deleted\":false},{\"id\":9,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"}," +
                    "{\"tagline\":\"Free your mind\",\"title\":\"The Matrix Reloaded\",\"released\":2003}]," +
                    "\"meta\":[{\"id\":5,\"type\":\"node\",\"deleted\":false},{\"id\":9,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"}," +
                    "{\"tagline\":\"Everything that has a beginning has an end\",\"title\":\"The Matrix Revolutions\",\"released\":2003}]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\",\"deleted\":false},{\"id\":10,\"type\":\"node\",\"deleted\":false}]}]}]," +
                "\"errors\":[]}";
    }

    @Test
    public void translateVarPropertyVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), conf.mapProperty("name"), Var.alloc("o"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"born\":1964,\"name\":\"Keanu Reeves\"},\"Keanu Reeves\"],\"meta\":[{\"id\":1,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Carrie-Anne Moss\"},\"Carrie-Anne Moss\"],\"meta\":[{\"id\":2,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1961,\"name\":\"Laurence Fishburne\"},\"Laurence Fishburne\"],\"meta\":[{\"id\":3,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1960,\"name\":\"Hugo Weaving\"},\"Hugo Weaving\"],\"meta\":[{\"id\":4,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"},\"Andy Wachowski\"],\"meta\":[{\"id\":5,\"type\":\"node\",\"deleted\":false},null]}]}]" +
                ",\"errors\":[]}";
    }

    @Test
    public void translateVarVarLabelTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), conf.mapNodeLabel("Person"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"born\":1964,\"name\":\"Keanu Reeves\"},\"label\"],\"meta\":[{\"id\":1,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Carrie-Anne Moss\"},\"label\"],\"meta\":[{\"id\":2,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1961,\"name\":\"Laurence Fishburne\"},\"label\"],\"meta\":[{\"id\":3,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1960,\"name\":\"Hugo Weaving\"},\"label\"],\"meta\":[{\"id\":4,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"},\"label\"],\"meta\":[{\"id\":5,\"type\":\"node\",\"deleted\":false},null]}]}]," +
                "\"errors\":[]}";
    }

    @Test
    public void translateVarVarNodeTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), conf.mapNode(node9));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"born\":1952,\"name\":\"Joel Silver\"},{}]," +
                    "\"meta\":[{\"id\":7,\"type\":\"node\",\"deleted\":false},{\"id\":14,\"type\":\"relationship\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1965,\"name\":\"Lana Wachowski\"},{}]," +
                    "\"meta\":[{\"id\":6,\"type\":\"node\",\"deleted\":false},{\"id\":13,\"type\":\"relationship\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1967,\"name\":\"Andy Wachowski\"},{}]," +
                    "\"meta\":[{\"id\":5,\"type\":\"node\",\"deleted\":false},{\"id\":12,\"type\":\"relationship\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1960,\"name\":\"Hugo Weaving\"},{\"roles\":[\"Agent Smith\"]}]," +
                    "\"meta\":[{\"id\":4,\"type\":\"node\",\"deleted\":false},{\"id\":11,\"type\":\"relationship\",\"deleted\":false}]}," +
                "{\"row\":[{\"born\":1961,\"name\":\"Laurence Fishburne\"},{\"roles\":[\"Morpheus\"]}]," +
                    "\"meta\":[{\"id\":3,\"type\":\"node\",\"deleted\":false},{\"id\":10,\"type\":\"relationship\",\"deleted\":false}]}]}]" +
                ",\"errors\":[]}";
    }

    @Test
    public void translateVarVarLiteral() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), NodeFactory.createLiteral("The Matrix"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[{\"edge\":{\"roles\":[\"Shane Falco\"]},\"source\":{\"born\":1964,\"name\":\"Keanu Reeves\"}," +
                    "\"target\":{\"tagline\":\"Pain heals, Chicks dig scars... Glory lasts forever\",\"title\":\"The Replacements\",\"released\":2000}},[]]," +
                    "\"meta\":[{\"id\":114,\"type\":\"relationship\",\"deleted\":false},{\"id\":1,\"type\":\"node\",\"deleted\":false},{\"id\":87,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"edge\":{\"roles\":[\"Johnny Mnemonic\"]},\"source\":{\"born\":1964,\"name\":\"Keanu Reeves\"}," +
                    "\"target\":{\"tagline\":\"The hottest data on earth. In the coolest head in town\",\"title\":\"Johnny Mnemonic\",\"released\":1995}},[]]," +
                    "\"meta\":[{\"id\":132,\"type\":\"relationship\",\"deleted\":false},{\"id\":1,\"type\":\"node\",\"deleted\":false},{\"id\":100,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"tagline\":\"Welcome to the Real World\",\"title\":\"The Matrix\",\"released\":1999},[\"title\"]]," +
                    "\"meta\":[{\"id\":0,\"type\":\"node\",\"deleted\":false},null]}," +
                "{\"row\":[{\"born\":1964,\"name\":\"Keanu Reeves\"},[]]," +
                    "\"meta\":[{\"id\":1,\"type\":\"node\",\"deleted\":false}]}]}]," +
                "\"errors\":[]}";
    }

    @Test
    public void translateNodeVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(conf.mapNode(node22), Var.alloc("p"), Var.alloc("o"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\"],\"data\":[" +
                "{\"row\":[\"label\",\"Person\"],\"meta\":[null,null]}," +
                "{\"row\":[[\"born\",\"name\"],[1968,\"Cuba Gooding Jr.\"]],\"meta\":[null,null,null,null]}," +
                "{\"row\":[{\"roles\":[\"Albert Lewis\"]},{\"tagline\":\"After life there is more. The end is just the beginning.\",\"title\":\"What Dreams May Come\",\"released\":1998}]," +
                    "\"meta\":[{\"id\":74,\"type\":\"relationship\",\"deleted\":false},{\"id\":56,\"type\":\"node\",\"deleted\":false}]}]}]," +
                "\"errors\":[]}";
    }

    @Test
    public void translateVarVarVarTest() {
        final LPG2RDFConfiguration conf = new DefaultConfiguration();
        final Triple t = new Triple(Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Node>> translation =
                translator.translateTriplePattern(new TriplePatternImpl(t), conf);
        final String neo4jResponse = "{\"results\":[{\"columns\":[\"ret1\",\"ret2\",\"ret3\"],\"data\":[" +
                "{\"row\":[{\"born\":1964,\"name\":\"Keanu Reeves\"},{\"roles\":[\"Shane Falco\"]},{\"tagline\":\"Pain heals, Chicks dig scars... Glory lasts forever\",\"title\":\"The Replacements\",\"released\":2000}]," +
                    "\"meta\":[{\"id\":1,\"type\":\"node\",\"deleted\":false},{\"id\":114,\"type\":\"relationship\",\"deleted\":false},{\"id\":87,\"type\":\"node\",\"deleted\":false}]}," +
                "{\"row\":[{\"tagline\":\"Welcome to the Real World\",\"title\":\"The Matrix\",\"released\":1999},\"label\",\"Movie\"]," +
                    "\"meta\":[{\"id\":0,\"type\":\"node\",\"deleted\":false},null,null]}," +
                "{\"row\":[{\"tagline\":\"Welcome to the Real World\",\"title\":\"The Matrix\",\"released\":1999},[\"title\",\"tagline\",\"released\"],[\"The Matrix\",\"Welcome to the Real World\",1999]]," +
                    "\"meta\":[{\"id\":0,\"type\":\"node\",\"deleted\":false},null,null,null,null,null,null]}," +
                "{\"row\":[{\"edge\":{\"roles\":[\"Shane Falco\"]},\"source\":{\"born\":1964,\"name\":\"Keanu Reeves\"},\"target\":{\"tagline\":\"Pain heals, Chicks dig scars... Glory lasts forever\",\"title\":\"The Replacements\",\"released\":2000}},[\"roles\"],[[\"Shane Falco\"]]]," +
                    "\"meta\":[{\"id\":114,\"type\":\"relationship\",\"deleted\":false},{\"id\":1,\"type\":\"node\",\"deleted\":false},{\"id\":87,\"type\":\"node\",\"deleted\":false},null,null]}]}]," +
                "\"errors\":[]}";
    }
}
