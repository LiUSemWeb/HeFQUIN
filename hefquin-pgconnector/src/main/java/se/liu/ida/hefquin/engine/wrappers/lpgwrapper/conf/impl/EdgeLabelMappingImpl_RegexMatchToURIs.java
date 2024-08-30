package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedEdgeLabelException;

public class EdgeLabelMappingImpl_RegexMatchToURIs extends EdgeLabelMappingImpl_AllToURIs {

    protected final String regex;

    public EdgeLabelMappingImpl_RegexMatchToURIs(final String regex, final String NSRELATIONSHIP){
        super(NSRELATIONSHIP);
        this.regex = regex;
    }

    @Override
    public Node map(final String label) {
        if (label.matches(regex)) {
            return super.map(label);
        }
        else {
            throw new UnSupportedEdgeLabelException("The given edge label (" + label + ") is not a supported label in the image of this edge label mapping.");
        }
    }
}
