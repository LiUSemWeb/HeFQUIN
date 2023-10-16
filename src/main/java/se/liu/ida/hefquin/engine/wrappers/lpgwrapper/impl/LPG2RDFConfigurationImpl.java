package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

public class LPG2RDFConfigurationImpl implements LPG2RDFConfiguration {

    protected final String NS = "https://example.org/";
    protected final String RELATIONSHIP = "relationship/";
    protected final String PROPERTY = "property/";

    protected final Node label;
    protected final NodeMapping nodeMapping;

    protected final NodeLabelMapping nodeLabelMapping;

    public LPG2RDFConfigurationImpl(final Node label, final NodeMapping nodeMapping, final NodeLabelMapping nodeLabelMapping){
        this.label = label;
        this.nodeMapping = nodeMapping;
        this.nodeLabelMapping = nodeLabelMapping;
    }

    @Override
    public Node mapNode(final LPGNode node) {
        return nodeMapping.map(node);
    }

    @Override
    public LPGNode unmapNode(final Node node) {
        return nodeMapping.unmap(node);
    }

    @Override
    public Node mapNodeLabel(final String label) {
        return nodeLabelMapping.map(label);
    }

    @Override
    public String unmapNodeLabel(final Node node) {
        return nodeLabelMapping.unmap(node);
    }

    @Override
    public Node mapEdgeLabel(final String label) {
        return NodeFactory.createURI(NS + RELATIONSHIP + label);
    }

    @Override
    public String unmapEdgeLabel(final Node node) {
        if (!node.isURI())
            throw new IllegalArgumentException("Default configuration only accepts URI Edge Labels");
        if (!node.getURI().startsWith(NS + RELATIONSHIP))
            throw new IllegalArgumentException("The provided URI is not mapping a Node Label");
        return node.getURI().replaceAll(NS + RELATIONSHIP, "");
    }

    @Override
    public Node mapProperty(final String property) {
        return NodeFactory.createURI(NS + PROPERTY + property);
    }

    @Override
    public String unmapProperty(final Node node) {
        if (!node.isURI())
            throw new IllegalArgumentException("Default configuration only accepts URI Property mappings");
        if (!node.getURI().startsWith(NS + PROPERTY))
            throw new IllegalArgumentException("The provided URI is not mapping a Property");
        return node.getURI().replaceAll(NS + PROPERTY, "");
    }

    @Override
    public Node getLabel() {
        return this.label;
    }

    @Override
    public boolean mapsToProperty(final Node n) {
        return n.isURI() && n.getURI().startsWith(NS + PROPERTY);
    }

    @Override
    public boolean isLabelIRI(final Node n) {
        return n.equals(label);
    }

    @Override
    public boolean mapsToLabel(final Node n) {
        return nodeLabelMapping.isPossibleResult(n);
    }

    @Override
    public boolean mapsToEdgeLabel(final Node n) {
        return n.isURI() && n.getURI().startsWith(NS + RELATIONSHIP);
    }

    @Override
    public boolean mapsToNode(final Node n) {
        return nodeMapping.isPossibleResult(n);
    }
}
