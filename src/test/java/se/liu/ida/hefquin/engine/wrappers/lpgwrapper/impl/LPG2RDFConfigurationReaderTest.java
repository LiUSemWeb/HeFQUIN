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
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
              + "PREFIX ex:     <http://example.org/>\n"
              + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
              + "\n"
              + "ex:LPGtoRDFConfig\n"
              + "   a  lr:LPGtoRDFConfiguration ;\n"
              + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
              + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
              + "   lr:nodeLabelMapping ex:IRINodeLabelMapping ;\n"
              + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
              + "\n"
              + "ex:IRINodeLabelMapping\n"
              + "   a  lr:IRIBasedNodeLabelMapping ;\n"
              + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
              + "\n"
              + "ex:IRINodeEdgeLabelMapping\n"
              + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
              + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
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

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.mapNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final Node resultNodeEdgeLabel = lpg2RDFConfiguration.mapEdgeLabel(label);
        assertNotNull(resultNodeEdgeLabel);
        assertTrue(resultNodeEdgeLabel.isURI());
        assertEquals(resultNodeEdgeLabel.getURI(), "https://example.org/relationship/0");
    }

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndLiteralBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:LiteralNodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
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

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.mapNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isLiteral());
        assertEquals(resultNodeLabel.getLiteral().toString(), "0");

        final Node resultNodeEdgeLabel = lpg2RDFConfiguration.mapEdgeLabel(label);
        assertNotNull(resultNodeEdgeLabel);
        assertTrue(resultNodeEdgeLabel.isURI());
        assertEquals(resultNodeEdgeLabel.getURI(), "https://example.org/relationship/0");
    }

    @Test
    public void LPG2RDFConfigWithBNodeBasedNodeMappingAndIRIBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:BNodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:IRINodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
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

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.mapNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final Node resultNodeEdgeLabel = lpg2RDFConfiguration.mapEdgeLabel(label);
        assertNotNull(resultNodeEdgeLabel);
        assertTrue(resultNodeEdgeLabel.isURI());
        assertEquals(resultNodeEdgeLabel.getURI(), "https://example.org/relationship/0");
    }

    @Test
    public void LPG2RDFConfigWithBNodeBasedNodeMappingAndLiteralBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:BNodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:LiteralNodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
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

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.mapNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isLiteral());
        assertEquals(resultNodeLabel.getLiteral().toString(), "0");

        final Node resultNodeEdgeLabel = lpg2RDFConfiguration.mapEdgeLabel(label);
        assertNotNull(resultNodeEdgeLabel);
        assertTrue(resultNodeEdgeLabel.isURI());
        assertEquals(resultNodeEdgeLabel.getURI(), "https://example.org/relationship/0");
    }

    @Test
    public void LPG2RDFConfigWithNodeMappingAsIRIBasedNodeMappingAndNodeLabelMappingAsIRIBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:IRINodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:NodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:NodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
        final LPG2RDFConfiguration lpg2RDFConfiguration = LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabel().isURI());
        assert(lpg2RDFConfiguration.getLabel().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.mapNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.mapNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final Node resultNodeEdgeLabel = lpg2RDFConfiguration.mapEdgeLabel(label);
        assertNotNull(resultNodeEdgeLabel);
        assertTrue(resultNodeEdgeLabel.isURI());
        assertEquals(resultNodeEdgeLabel.getURI(), "https://example.org/relationship/0");
    }

    /*
     * In this test case, LabelPredicate is not a URI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void InvalidLabelLPG2RDFConfigWithIRIBasedNodeMappingAndLiteralBasedNodeLabelMapping() {
        final String turtle =
                          "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate 'Hello World!' ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:LiteralNodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
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
     * In this test case, prefixOfIRIs of NodeMapping is not a URI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void InvalidNodeMappingPrefixOfIRIsLPG2RDFConfigWithIRIBasedNodeMappingAndLiteralBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:LiteralNodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
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
     * In this test case, prefixOfIRIs of NodeLabelMapping is not a URI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void InvalidNodeLabelMappingPrefixOfIRIsLPG2RDFConfigWithBNodeBasedNodeMappingAndIRIBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:BNodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:IRINodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"Hello World!\" ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }

    /*
     * In this test case, prefixOfIRIs of NodeEdgeLabelMapping is not a URI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void InvalidNodeEdgeLabelMappingPrefixOfIRIsLPG2RDFConfigWithBNodeBasedNodeMappingAndIRIBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:BNodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:IRINodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"Hello World!\" ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }

    /*
     * In this test case, prefixOfIRIs is not given in IRINodeMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithIRIBasedNodeLabelMappingAndIRIBasedNodeMappingWithoutPrefixOfIRIs() {
        final String turtle =
                          "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:IRINodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
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
     * In this test case, prefixOfIRIs is not given in IRINodeLabelMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingWithoutPrefixOfIRIs() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:IRINodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
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
     * In this test case, prefixOfIRIs is not given in IRINodeEdgeLabelMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingWithoutPrefixOfIRIsForNodeEdgeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:IRINodeLabelMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping ex:IRINodeEdgeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ."
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
     * In this test case, the LPGtoRDFConfiguration is not given.
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

    /*
     * In this test case, there are more than one instances of NodeMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithMultipleNodeMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeMapping  ex:BNodeMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping  ex:IRINodeEdgeLabelMapping ;\n"
                        + "   lr:nodeLabelMapping ex:LiteralNodeLabelMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:NodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }

    /*
     * In this test case, there are more than one instances of NodeLabelMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithMultipleNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:BNodeMapping ;\n"
                        + "   lr:nodeEdgeLabelMapping  ex:IRINodeEdgeLabelMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:IRINodeLabelMapping ;\n"
                        + "   lr:nodeLabelMapping ex:LiteralNodeLabelMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeEdgeLabelMapping\n"
                        + "   a  lr:IRIBasedNodeEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        LPG2RDFConfigurationReader.readFromModel(lpg2rdf);
    }
}
