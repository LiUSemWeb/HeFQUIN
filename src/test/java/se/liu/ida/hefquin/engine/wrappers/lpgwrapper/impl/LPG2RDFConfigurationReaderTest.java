package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;


import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.Test;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

import static org.junit.Assert.*;

public class LPG2RDFConfigurationReaderTest {

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
              + "PREFIX ex:     <http://example.org/>\n"
              + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
              + "\n"
              + "ex:LPGtoRDFConfig\n"
              + "   a  lr:LPGtoRDFConfiguration ;\n"
              + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
              + "   lr:nodeMapping  ex:IRINodeMapping ."
              + "\n"
              + "ex:IRINodeMapping\n"
              + "   a  lr:IRIBasedNodeMapping ;\n"
              + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabel().isURI());
        assert(lpg2RDFConfiguration.getLabel().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.mapNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");
    }


    @Test
    public void LPG2RDFConfigWithBNodeBasedNodeMapping() {
        final String turtle =
                  "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                + "PREFIX ex:     <http://example.org/>\n"
                + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                + "\n"
                + "ex:LPGtoRDFConfig\n"
                + "   a  lr:LPGtoRDFConfiguration ;\n"
                + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                + "   lr:nodeMapping  ex:BNodeMapping ."
                + "\n"
                + "ex:BNodeMapping\n"
                + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabel().isURI());
        final LPGNode node = new LPGNode("0", "", null);
        final Node resultNode = lpg2RDFConfiguration.mapNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isBlank());
        assertEquals(resultNode.getBlankNodeId().toString(), "0");
    }

    @Test
    public void LPG2RDFConfigWithNodeMappingAsBNodeBasedNodeMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:BNodeMapping ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }

    @Test
    public void LPG2RDFConfigWithNodeMappingAsIRIBasedNodeMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:NodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }

    /*
    In this test case, prefixOfIRIs is not a URI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void InvalidLabelLPG2RDFConfigWithIRIBasedNodeMapping() {
        final String turtle =
                          "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"Hello World!\" .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }

    /*
    In this test case, LabelPredicate is not a URI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void InvalidPrefixOfIRIsLPG2RDFConfigWithIRIBasedNodeMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate 'Hello World!' ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }

    /*
    In this test case, prefixOfIRIs is not given in IRINodeMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithIRIBasedNodeMappingWithoutPrefixOfIRIs() {
        final String turtle =
                          "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }

    /*
    In this test case, the LPGtoRDFConfiguration is not given.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFWrongConfig() {
        final String turtle =
                          "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }
}
