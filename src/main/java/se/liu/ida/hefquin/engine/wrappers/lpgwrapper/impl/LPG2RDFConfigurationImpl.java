package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

public class LPG2RDFConfigurationImpl implements LPG2RDFConfiguration {

    protected final Node label;
    protected final NodeMapping nodeMapping;

    protected final NodeLabelMapping nodeLabelMapping;
    protected final EdgeLabelMapping edgeLabelMapping;
    protected final PropertyNameMapping propertyNameMapping;

    public LPG2RDFConfigurationImpl(final Node label, final NodeMapping nodeMapping, final NodeLabelMapping nodeLabelMapping,
                                    final EdgeLabelMapping edgeLabelMapping, final PropertyNameMapping propertyNameMapping){
        this.label = label;
        this.nodeMapping = nodeMapping;
        this.nodeLabelMapping = nodeLabelMapping;
        this.edgeLabelMapping = edgeLabelMapping;
        this.propertyNameMapping=propertyNameMapping;
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
        return edgeLabelMapping.map(label);
    }

    @Override
    public String unmapEdgeLabel(final Node node) {
        return edgeLabelMapping.unmap(node);
    }

    @Override
    public Node mapProperty(final String propertyName) {
        return propertyNameMapping.map(propertyName);
    }

    @Override
    public String unmapProperty(final Node node) {
        return propertyNameMapping.unmap(node);
    }

    @Override
    public Node getLabel() {
        return this.label;
    }

    @Override
    public boolean mapsToProperty(final Node n) {
        return propertyNameMapping.isPossibleResult(n);
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
        return edgeLabelMapping.isPossibleResult(n);
    }

    @Override
    public boolean mapsToNode(final Node n) {
        return nodeMapping.isPossibleResult(n);
    }
}
