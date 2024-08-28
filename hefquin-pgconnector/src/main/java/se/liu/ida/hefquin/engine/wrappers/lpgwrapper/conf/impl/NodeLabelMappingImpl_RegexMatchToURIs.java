package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedNodeLabelException;

public class NodeLabelMappingImpl_RegexMatchToURIs extends NodeLabelMappingImpl_AllToURIs {

    protected final String regex;

    public NodeLabelMappingImpl_RegexMatchToURIs(final String regex, final String NSNODELABEL){
        super(NSNODELABEL);
        this.regex = regex;
    }

    @Override
    public Node map(final String label) {
        if (label.matches(regex)) {
            return super.map(label);
        }
        else {
            throw new UnSupportedNodeLabelException("The given node label (" + label + ") is not a supported label in the image of this node label mapping.");
        }
    }
}
