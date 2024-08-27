package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

import static org.junit.Assert.*;
import org.junit.Test;

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
              + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
              + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
              + "\n"
              + "ex:IRINodeLabelMapping\n"
              + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
              + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
              + "\n"
              + "ex:IRIEdgeLabelMapping\n"
              + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
              + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
              + "\n"
              + "ex:IRIPropertyNameMapping\n"
              + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
              + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
              + "\n"
              + "ex:IRINodeMapping\n"
              + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
              + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
    }

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndRegexIRIBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping ex:RegexIRINodeLabelMapping ;\n"
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:RegexIRINodeLabelMapping\n"
                        + "   a  lr:RegexBasedNodeLabelMapping ;\n"
                        + "   lr:regex  \"[0-9]+\" ;\n"
                        + "   lr:prefixOfIRIs  \"https://test.org/test/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://test.org/test/0");

        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
    }

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndSingleIRIBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping ex:SingleIRINodeLabelMapping ;\n"
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:SingleIRINodeLabelMapping\n"
                        + "   a  lr:SingletonIRINodeLabelMapping ;\n"
                        + "   lr:label \"DIRECTED\" ;\n"
                        + "   lr:iri \"http://singleExample.org/directorOf\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "DIRECTED";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "http://singleExample.org/directorOf");

        final String edgeLabel = "0";
        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(edgeLabel);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
    }

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndSingleLiteralBasedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping ex:SingleLiteralNodeLabelMapping ;\n"
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:SingleLiteralNodeLabelMapping\n"
                        + "   a  lr:SingletonLiteralNodeLabelMapping ;\n"
                        + "   lr:label \"DIRECTED\" ;\n"
                        + "   lr:literal \"directorOf\" ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "DIRECTED";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isLiteral());
        assertEquals(resultNodeLabel.getLiteral().toString(), "directorOf");

        final String edgeLabel = "0";
        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(edgeLabel);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
    }

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndCombinedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping ex:CombinedNodeLabelMapping ;\n"
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:RegexIRINodeLabelMapping\n"
                        + "   a  lr:RegexBasedNodeLabelMapping ;\n"
                        + "   lr:regex  \"^[0-9]+\" ;\n"
                        + "   lr:prefixOfIRIs  \"https://test.org/test/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:SingleIRINodeLabelMapping\n"
                        + "   a  lr:SingletonIRINodeLabelMapping ;\n"
                        + "   lr:label \"DIRECTED\" ;\n"
                        + "   lr:iri \"http://singleExample.org/directorOf\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:SingleLiteralNodeLabelMapping\n"
                        + "   a  lr:SingletonLiteralNodeLabelMapping ;\n"
                        + "   lr:label \"ACTED\" ;\n"
                        + "   lr:literal \"actorOf\" ."
                        + "\n"
                        + "ex:CombinedNodeLabelMapping\n"
                        + "   a  lr:CompositeNodeLabelMapping ;\n"
                        + "   lr:componentMappings (ex:SingleIRINodeLabelMapping ex:SingleLiteralNodeLabelMapping ex:RegexIRINodeLabelMapping ex:IRINodeLabelMapping) ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");


        String nodeLabel = "DIRECTED";
        Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(nodeLabel);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "http://singleExample.org/directorOf");

        nodeLabel = "ACTED";
        resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(nodeLabel);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isLiteral());
        assertEquals(resultNodeLabel.getLiteral().toString(), "actorOf");

        nodeLabel = "0";
        resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(nodeLabel);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://test.org/test/0");

        nodeLabel = "ACTED_IN";
        resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(nodeLabel);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/ACTED_IN");

        final String label = "0";
        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
    }

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingAndRegexBasedEdgeLabelMapping() {
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
                        + "   lr:edgeLabelMapping ex:RegexEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:RegexEdgeLabelMapping\n"
                        + "   a  lr:RegexBasedEdgeLabelMapping ;\n"
                        + "   lr:regex  \"[0-9]+\" ;\n"
                        + "   lr:prefixOfIRIs  \"https://test.org/test/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://test.org/test/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
    }

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingAndSingleEdgeLabelMapping() {
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
                        + "   lr:edgeLabelMapping ex:SingleIRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:SingleIRIEdgeLabelMapping\n"
                        + "   a  lr:SingletonIRIEdgeLabelMapping ;\n"
                        + "   lr:label \"DIRECTED\" ;\n"
                        + "   lr:iri \"http://singleExample.org/directorOf\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final String edgeLabel = "DIRECTED";
        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(edgeLabel);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "http://singleExample.org/directorOf");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
    }

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingAndCombinedEdgeLabelMapping() {
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
                        + "   lr:edgeLabelMapping ex:CombinedIRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:RegexEdgeLabelMapping\n"
                        + "   a  lr:RegexBasedEdgeLabelMapping ;\n"
                        + "   lr:regex  \"^[0-9]+\" ;\n"
                        + "   lr:prefixOfIRIs  \"https://test.org/test/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:SingleIRIEdgeLabelMapping\n"
                        + "   a  lr:SingletonIRIEdgeLabelMapping ;\n"
                        + "   lr:label \"DIRECTED\" ;\n"
                        + "   lr:iri \"http://singleExample.org/directorOf\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:CombinedIRIEdgeLabelMapping\n"
                        + "   a  lr:CompositeEdgeLabelMapping ;\n"
                        + "   lr:componentMappings (ex:SingleIRIEdgeLabelMapping ex:RegexEdgeLabelMapping ex:IRIEdgeLabelMapping) ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        String edgeLabel = "DIRECTED";
        Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(edgeLabel);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "http://singleExample.org/directorOf");

        edgeLabel = "0";
        resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(edgeLabel);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://test.org/test/0");

        edgeLabel = "ACTED_IN";
        resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(edgeLabel);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/ACTED_IN");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isLiteral());
        assertEquals(resultNodeLabel.getLiteral().toString(), "0");

        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        final LPGNode node = new LPGNode("0", "", null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isBlank());
        assertEquals(resultNode.getBlankNodeId().toString(), "0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        final LPGNode node = new LPGNode("0", "", null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isBlank());
        assertEquals(resultNode.getBlankNodeId().toString(), "0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isLiteral());
        assertEquals(resultNodeLabel.getLiteral().toString(), "0");

        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:NodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:NodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/0");
    }
    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingAndRegexBasedPropertyNameMapping() {
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:RegexPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:RegexPropertyNameMapping\n"
                        + "   a  lr:RegexBasedPropertyNameMapping ;\n"
                        + "   lr:regex  \"[0-9]+\" ;\n"
                        + "   lr:prefixOfIRIs  \"https://test.org/test/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "0";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://test.org/test/0");
    }
    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingAndSinglePropertyNameMapping() {
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:SingleIRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:SingleIRIPropertyNameMapping\n"
                        + "   a  lr:SingletonIRIPropertyNameMapping ;\n"
                        + "   lr:propertyName \"DIRECTED\" ;\n"
                        + "   lr:iri \"http://singleExample.org/directorOf\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");


        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        final String propertyName = "DIRECTED";
        final Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "http://singleExample.org/directorOf");
    }

    @Test
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingAndCombinedPropertyNameMapping() {
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:CombinedIRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:RegexPropertyNameMapping\n"
                        + "   a  lr:RegexBasedPropertyNameMapping ;\n"
                        + "   lr:regex  \"^[0-9]+\" ;\n"
                        + "   lr:prefixOfIRIs  \"https://test.org/test/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:SingleIRIPropertyNameMapping\n"
                        + "   a  lr:SingletonIRIPropertyNameMapping ;\n"
                        + "   lr:propertyName \"DIRECTED\" ;\n"
                        + "   lr:iri \"http://singleExample.org/directorOf\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:CombinedIRIPropertyNameMapping\n"
                        + "   a  lr:CompositePropertyNameMapping ;\n"
                        + "   lr:componentMappings (ex:SingleIRIPropertyNameMapping ex:RegexPropertyNameMapping ex:IRIPropertyNameMapping) ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        final LPG2RDFConfiguration lpg2RDFConfiguration = new LPG2RDFConfigurationReader().read(lpg2rdf);
        assert(lpg2RDFConfiguration.getLabelPredicate().isURI());
        assert(lpg2RDFConfiguration.getLabelPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label"));
        final LPGNode node = new LPGNode("0", null, null);
        final Node resultNode = lpg2RDFConfiguration.getRDFTermForLPGNode(node);
        assertNotNull(resultNode);
        assertTrue(resultNode.isURI());
        assertEquals(resultNode.getURI(), "https://example.org/node/0");

        final String label = "0";
        final Node resultNodeLabel = lpg2RDFConfiguration.getRDFTermForNodeLabel(label);
        assertNotNull(resultNodeLabel);
        assertTrue(resultNodeLabel.isURI());
        assertEquals(resultNodeLabel.getURI(), "https://example.org/label/0");

        final Node resultEdgeLabel = lpg2RDFConfiguration.getIRIForEdgeLabel(label);
        assertNotNull(resultEdgeLabel);
        assertTrue(resultEdgeLabel.isURI());
        assertEquals(resultEdgeLabel.getURI(), "https://example.org/relationship/0");

        String propertyName = "DIRECTED";
        Node resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "http://singleExample.org/directorOf");

        propertyName = "0";
        resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://test.org/test/0");

        propertyName = "ACTED_IN";
        resultPropertyName = lpg2RDFConfiguration.getIRIForPropertyName(propertyName);
        assertNotNull(resultPropertyName);
        assertTrue(resultPropertyName.isURI());
        assertEquals(resultPropertyName.getURI(), "https://example.org/property/ACTED_IN");
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"Hello World!\" .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"Hello World!\" ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
    }


    /*
     * In this test case, prefixOfIRIs of EdgeLabelMapping is not a URI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void InvalidEdgeLabelMappingPrefixOfIRIsLPG2RDFConfigWithBNodeBasedNodeMappingAndIRIBasedNodeLabelMapping() {
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"Hello World!\" ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
    }


    /*
     * In this test case, prefixOfIRIs of propertyNameMapping is not a URI.
     */
    @Test(expected = IllegalArgumentException.class)
    public void InvalidPropertyNameMappingPrefixOfIRIsLPG2RDFConfigWithBNodeBasedNodeMappingAndIRIBasedNodeLabelMapping() {
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"Hello World!\" ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/propertyName/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
    }

    /*
     * In this test case, prefixOfIRIs is not given in IRIEdgeLabelMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingWithoutPrefixOfIRIsForEdgeLabelMapping() {
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
    }

    /*
     * In this test case, prefixOfIRIs is not given in IRIPropertyNameMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingWithoutPrefixOfIRIsForPropertyNameMapping() {
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
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
                        + "   a  lr:IRIPrefixBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
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
                        + "   lr:edgeLabelMapping  ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping  ex:IRIPropertyNameMapping ;\n"
                        + "   lr:nodeLabelMapping ex:LiteralNodeLabelMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:NodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
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
                        + "   lr:edgeLabelMapping  ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping  ex:IRIPropertyNameMapping ;\n"
                        + "   lr:nodeLabelMapping  ex:IRINodeLabelMapping ;\n"
                        + "   lr:nodeLabelMapping ex:LiteralNodeLabelMapping ."
                        + "\n"
                        + "ex:LiteralNodeLabelMapping\n"
                        + "   a  lr:LiteralBasedNodeLabelMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:BNodeMapping\n"
                        + "   a  lr:BNodeBasedNodeMapping .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
    }

    /*
     * In this test case, there is an instance of CombinedNodeLabelMapping inside the nodeLabelMappings list of another CombinedNodeLabelMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndNestedCombinedNodeLabelMapping() {
        final String turtle =
                "PREFIX lr:     <http://www.example.org/se/liu/ida/hefquin/lpg2rdf#>\n"
                        + "PREFIX ex:     <http://example.org/>\n"
                        + "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>\n"
                        + "\n"
                        + "ex:LPGtoRDFConfig\n"
                        + "   a  lr:LPGtoRDFConfiguration ;\n"
                        + "   lr:labelPredicate  \"http://www.w3.org/2000/01/rdf-schema#label\"^^xsd:anyURI ;\n"
                        + "   lr:nodeMapping  ex:IRINodeMapping ;\n"
                        + "   lr:nodeLabelMapping ex:CombinedNodeLabelMapping ;\n"
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:RegexNodeLabelMapping\n"
                        + "   a  lr:RegexBasedNodeLabelMapping ;\n"
                        + "   lr:regex  \"^[0-9]+\" ;\n"
                        + "   lr:prefixOfIRIs  \"https://test.org/test/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:SingleIRINodeLabelMapping\n"
                        + "   a  lr:SingletonIRINodeLabelMapping ;\n"
                        + "   lr:label \"DIRECTED\" ;\n"
                        + "   lr:iri \"http://singleExample.org/directorOf\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:CombinedNodeLabelMapping\n"
                        + "   a  lr:CompositeNodeLabelMapping ;\n"
                        + "   lr:componentMappings (ex:InnerCombinedNodeLabelMapping) ."
                        + "\n"
                        + "ex:InnerCombinedNodeLabelMapping\n"
                        + "   a  lr:NodeLabelMapping ;\n"
                        + "   lr:componentMappings (ex:SingleIRINodeLabelMapping ex:RegexNodeLabelMapping ex:IRINodeLabelMapping) ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
    }

    /*
     * In this test case, there is an instance of CombinedEdgeLabelMapping inside the edgeLabelMappings list of another CombinedEdgeLabelMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingAndNestedCombinedEdgeLabelMapping() {
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
                        + "   lr:edgeLabelMapping ex:CombinedIRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:IRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:RegexEdgeLabelMapping\n"
                        + "   a  lr:RegexBasedEdgeLabelMapping ;\n"
                        + "   lr:regex  \"^[0-9]+\" ;\n"
                        + "   lr:prefixOfIRIs  \"https://test.org/test/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:SingleIRIEdgeLabelMapping\n"
                        + "   a  lr:SingletonIRIEdgeLabelMapping ;\n"
                        + "   lr:label \"DIRECTED\" ;\n"
                        + "   lr:iri \"http://singleExample.org/directorOf\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:CombinedIRIEdgeLabelMapping\n"
                        + "   a  lr:CompositeEdgeLabelMapping ;\n"
                        + "   lr:componentMappings (ex:InnerCombinedIRIEdgeLabelMapping) ."
                        + "\n"
                        + "ex:InnerCombinedIRIEdgeLabelMapping\n"
                        + "   a  lr:EdgeLabelMapping ;\n"
                        + "   lr:componentMappings (ex:SingleIRIEdgeLabelMapping ex:RegexEdgeLabelMapping ex:IRIEdgeLabelMapping) ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
    }

    /*
     * In this test case, there is an instance of CombinedPropertyNameMapping inside the propertyNameMappings list of another CombinedPropertyNameMapping.
     */
    @Test(expected = IllegalArgumentException.class)
    public void LPG2RDFConfigWithIRIBasedNodeMappingAndIRIBasedNodeLabelMappingAndNestedCombinedPropertyNameMapping() {
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
                        + "   lr:edgeLabelMapping ex:IRIEdgeLabelMapping ;\n"
                        + "   lr:propertyNameMapping ex:CombinedIRIPropertyNameMapping ."
                        + "\n"
                        + "ex:IRINodeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/label/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIEdgeLabelMapping\n"
                        + "   a  lr:IRIPrefixBasedEdgeLabelMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/relationship/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:IRIPropertyNameMapping\n"
                        + "   a  lr:IRIPrefixBasedPropertyNameMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/property/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:RegexPropertyNameMapping\n"
                        + "   a  lr:RegexBasedPropertyNameMapping ;\n"
                        + "   lr:regex  \"^[0-9]+\" ;\n"
                        + "   lr:prefixOfIRIs  \"https://test.org/test/\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:SingleIRIPropertyNameMapping\n"
                        + "   a  lr:SingletonIRIPropertyNameMapping ;\n"
                        + "   lr:propertyName \"DIRECTED\" ;\n"
                        + "   lr:iri \"http://singleExample.org/directorOf\"^^xsd:anyURI ."
                        + "\n"
                        + "ex:CombinedIRIPropertyNameMapping\n"
                        + "   a  lr:CompositePropertyNameMapping ;\n"
                        + "   lr:componentMappings (ex:InnerCombinedIRIPropertyNameMapping) ."
                        + "\n"
                        + "ex:InnerCombinedIRIPropertyNameMapping\n"
                        + "   a  lr:PropertyNameMapping ;\n"
                        + "   lr:componentMappings (ex:SingleIRIPropertyNameMapping ex:RegexPropertyNameMapping ex:IRIPropertyNameMapping) ."

                        + "\n"
                        + "ex:IRINodeMapping\n"
                        + "   a  lr:IRIPrefixBasedNodeMapping ;\n"
                        + "   lr:prefixOfIRIs  \"https://example.org/node/\"^^xsd:anyURI .";

        final Model lpg2rdf = ModelFactory.createDefaultModel();

        final RDFParserBuilder b = RDFParser.fromString(turtle);
        b.lang( Lang.TURTLE );
        b.parse(lpg2rdf);

        new LPG2RDFConfigurationReader().read(lpg2rdf);
    }
}
