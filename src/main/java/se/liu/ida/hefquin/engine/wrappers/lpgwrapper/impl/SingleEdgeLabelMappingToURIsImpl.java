package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class SingleEdgeLabelMappingToURIsImpl implements EdgeLabelMapping {

    protected final String label;
    protected final String iri;

    public SingleEdgeLabelMappingToURIsImpl(final String label, final String iri){
        this.label=label;
        this.iri=iri;
    }

    @Override
    public Node map(final String label) {
        if (label.equals(this.label)) {
            return NodeFactory.createURI(iri);
        }
        else {
            throw new IllegalArgumentException("The given edge label (" + label + ") is not a supported label in the image of this edge label mapping.");
        }
    }

    public String unmap(final Node node) {
        if (!isPossibleResult(node))
            throw new IllegalArgumentException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this edge label mapping.");
        return this.label;
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.isURI() && node.getURI().equals(this.iri);
    }

}
