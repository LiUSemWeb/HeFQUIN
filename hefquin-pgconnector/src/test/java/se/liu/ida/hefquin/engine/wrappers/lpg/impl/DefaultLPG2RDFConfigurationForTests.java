package se.liu.ida.hefquin.engine.wrappers.lpg.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNode;

public class DefaultLPG2RDFConfigurationForTests implements LPG2RDFConfiguration {

    protected final String NS = "https://example.org/";
    protected final String NODE = "node/";
    protected final String NODELABEL = "label/";
    protected final String RELATIONSHIP = "relationship/";
    protected final String PROPERTY = "property/";
    protected final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

    @Override
    public Node getRDFTermForLPGNode(final LPGNode node) {
        return NodeFactory.createURI(NS + NODE + node.getId());
    }

    @Override
    public LPGNode getLPGNodeForRDFTerm(final Node node) {
        if (!node.isURI())
            throw new IllegalArgumentException("Default configuration only accepts URI Node mappings");
        if (!node.getURI().startsWith(NS + NODE))
            throw new IllegalArgumentException("The provided URI is not mapping a Node");
        final String id = node.getURI().replaceAll(NS + NODE, "");
        return new LPGNode(id, "", null);
    }

    @Override
    public Node getRDFTermForNodeLabel(final String label) {
        return NodeFactory.createURI(NS + NODELABEL + label);
    }

    @Override
    public String getNodeLabelForRDFTerm(final Node node) {
        if (!node.isURI())
            throw new IllegalArgumentException("Default configuration only accepts URI Node Labels");
        if (!node.getURI().startsWith(NS + NODELABEL))
            throw new IllegalArgumentException("The provided URI is not mapping a Node Label");
        return node.getURI().replaceAll(NS + NODELABEL, "");
    }

    @Override
    public Node getIRIForEdgeLabel(final String label) {
        return NodeFactory.createURI(NS + RELATIONSHIP + label);
    }

    @Override
    public String getEdgeLabelForIRI(final Node node) {
        if (!node.isURI())
            throw new IllegalArgumentException("Default configuration only accepts URI Edge Labels");
        if (!node.getURI().startsWith(NS + RELATIONSHIP))
            throw new IllegalArgumentException("The provided URI is not mapping a Node Label");
        return node.getURI().replaceAll(NS + RELATIONSHIP, "");
    }

    @Override
    public Node getIRIForPropertyName(final String property) {
        return NodeFactory.createURI(NS + PROPERTY + property);
    }

    @Override
    public String getPropertyNameForIRI(final Node node) {
        if (!node.isURI())
            throw new IllegalArgumentException("Default configuration only accepts URI Property mappings");
        if (!node.getURI().startsWith(NS + PROPERTY))
            throw new IllegalArgumentException("The provided URI is not mapping a Property");
        return node.getURI().replaceAll(NS + PROPERTY, "");
    }

    @Override
    public Node getLabelPredicate() {
        return NodeFactory.createURI(LABEL);
    }

    @Override
    public boolean isIRIForPropertyName(final Node n) {
        return n.isURI() && n.getURI().startsWith(NS + PROPERTY);
    }

    @Override
    public boolean isRDFTermForNodeLabel(final Node n) {
        return n.isURI() && n.getURI().startsWith(NS + NODELABEL);
    }

    @Override
    public boolean isIRIForEdgeLabel(final Node n) {
        return n.isURI() && n.getURI().startsWith(NS + RELATIONSHIP);
    }

    @Override
    public boolean isRDFTermForLPGNode(final Node n) {
        return n.isURI() && n.getURI().startsWith(NS + NODE);
    }
}
