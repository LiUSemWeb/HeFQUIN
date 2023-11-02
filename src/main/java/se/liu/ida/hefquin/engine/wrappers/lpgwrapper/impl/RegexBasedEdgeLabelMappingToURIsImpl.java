package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class RegexBasedEdgeLabelMappingToURIsImpl implements EdgeLabelMapping {

    protected final String NSRELATIONSHIP;
    protected final String regex;


    public RegexBasedEdgeLabelMappingToURIsImpl(final String regex, final String NSRELATIONSHIP){
        this.NSRELATIONSHIP = NSRELATIONSHIP ;
        this.regex = regex;
    }

    @Override
    public Node map(final String label) {
        if (label.matches(regex)) {
            return NodeFactory.createURI(NSRELATIONSHIP + label);
        }
        else {
            throw new IllegalArgumentException("The given edge label (" + label + ") is not a supported label in the image of this edge label mapping.");
        }
    }

    @Override
    public String unmap(final Node node) {
        if (!isPossibleResult(node))
            throw new IllegalArgumentException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this edge label mapping.");
        return node.getURI().replaceAll(NSRELATIONSHIP, "");
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.isURI() && node.getURI().startsWith(NSRELATIONSHIP);
    }
}
