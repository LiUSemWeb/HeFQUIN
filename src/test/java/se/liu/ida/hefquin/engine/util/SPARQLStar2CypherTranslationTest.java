package se.liu.ida.hefquin.engine.util;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.*;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.data.utils.TriplesToSolMapsConverter;
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
                "MATCH (cpvar1) WHERE cpvar1.name = 'Quentin Tarantino' RETURN nm(cpvar1) AS s UNION " +
                        "MATCH ()-[cpvar2]->() WHERE cpvar2.name = 'Quentin Tarantino' RETURN elm(cpvar2) AS s");
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
                        "RETURN nm(cpvar1) AS s, cpvar1.name AS o UNION " +
                        "MATCH ()-[cpvar2]->() WHERE EXISTS(cpvar2.name) " +
                        "RETURN elm(cpvar2) AS s, cpvar2.name AS o");
    }

}
