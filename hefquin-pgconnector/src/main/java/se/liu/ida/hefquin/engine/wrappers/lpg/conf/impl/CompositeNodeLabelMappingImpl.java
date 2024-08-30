package se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.engine.wrappers.lpg.impl.exceptions.UnSupportedNodeLabelException;

import java.util.List;

public class CompositeNodeLabelMappingImpl implements NodeLabelMapping {

    protected final List<NodeLabelMapping> nodeLabelMappings;

    public CompositeNodeLabelMappingImpl(final List<NodeLabelMapping> nodeLabelMappings){
        this.nodeLabelMappings = nodeLabelMappings;
    }

    @Override
    public Node map(final String label) {
        for (final NodeLabelMapping nodeLabelMapping : nodeLabelMappings) {
            try {
                return nodeLabelMapping.map(label);
            }
            catch (final UnSupportedNodeLabelException e) {}
        }
        throw new UnSupportedNodeLabelException("The given node label (" + label + ") is not a supported label in the image of this node label mapping.");
    }

    public String unmap(final Node node) {
        for (final NodeLabelMapping nodeLabelMapping : nodeLabelMappings) {
            try {
                return nodeLabelMapping.unmap(node);
            }
            catch (final UnSupportedNodeLabelException e) {}
        }
        throw new UnSupportedNodeLabelException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this node label mapping.");
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        for (final NodeLabelMapping nodeLabelMapping : nodeLabelMappings) {
                if(nodeLabelMapping.isPossibleResult(node)){
                    return true;
                }
        }
        return false;
    }
}
