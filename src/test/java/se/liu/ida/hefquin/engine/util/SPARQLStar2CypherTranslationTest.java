package se.liu.ida.hefquin.engine.util;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Configurations;
import se.liu.ida.hefquin.engine.utils.SPARQLStar2CypherTranslator;

import static org.junit.Assert.assertEquals;

public class SPARQLStar2CypherTranslationTest {

    @Test
    public void testVariablePropertyLiteral() {
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(Configurations.PROPERTY_MAPPING+"name"),
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
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(Configurations.LABEL_URI),
                NodeFactory.createURI(Configurations.CLASS_MAPPING+"Person"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) WHERE cpvar1:Person RETURN nm(cpvar1) AS s");
    }

    @Test
    public void testVarRelationshipURI() {
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(Configurations.RELATIONSHIP_MAPPING+"DIRECTED"),
                NodeFactory.createURI(Configurations.NODE_MAPPING+22));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1)-[:DIRECTED]->(cpvar2) " +
                        "WHERE ID(cpvar2) = 22 RETURN nm(cpvar1) AS s");
    }

    @Test
    public void testVarLabelVar() {
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(Configurations.LABEL_URI),
                Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) RETURN nm(cpvar1) AS s, labels(cpvar1) AS o");
    }

    @Test
    public void testVarRelationshipVar() {
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(Configurations.RELATIONSHIP_MAPPING+"DIRECTED"),
                Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1)-[:DIRECTED]->(cpvar2) RETURN nm(cpvar1) AS s, nm(cpvar2) AS o");
    }

    @Test
    public void testVarPropertyVar() {
        final Triple t = new Triple(Var.alloc("s"),
                NodeFactory.createURI(Configurations.PROPERTY_MAPPING+"name"),
                Var.alloc("o"));
        final String translation = SPARQLStar2CypherTranslator.translate(new BGPImpl(new TriplePatternImpl(t)));
        assertEquals(translation,
                "MATCH (cpvar1) WHERE EXISTS(cpvar1.name) " +
                        "RETURN nm(cpvar1) AS r1, '' AS r2, '' AS r3, cpvar1.name AS o UNION " +
                        "MATCH (cpvar2)-[cpvar3]->(cpvar4) WHERE EXISTS(cpvar3.name) " +
                        "RETURN nm(cpvar2) AS r1, elm(cpvar3) AS r2, nm(cpvar4) AS r3, cpvar3.name AS o");
    }

}
