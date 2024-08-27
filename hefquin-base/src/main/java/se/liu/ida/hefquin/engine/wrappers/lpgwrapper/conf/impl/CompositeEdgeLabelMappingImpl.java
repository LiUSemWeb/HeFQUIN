package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedEdgeLabelException;

import java.util.List;

public class CompositeEdgeLabelMappingImpl implements EdgeLabelMapping {

    protected final List<EdgeLabelMapping> edgeLabelMappings;

    public CompositeEdgeLabelMappingImpl(final List<EdgeLabelMapping> edgeLabelMappings){
        this.edgeLabelMappings = edgeLabelMappings;
    }

    @Override
    public Node map(final String label) {
        for (final EdgeLabelMapping edgeLabelMapping : edgeLabelMappings) {
            try {
                return edgeLabelMapping.map(label);
            }
            catch (final UnSupportedEdgeLabelException e) {}
        }
        throw new UnSupportedEdgeLabelException("The given edge label (" + label + ") is not a supported label in the image of this edge label mapping.");
    }

    public String unmap(final Node node) {
        for (final EdgeLabelMapping edgeLabelMapping : edgeLabelMappings) {
            try {
                return edgeLabelMapping.unmap(node);
            }
            catch (final UnSupportedEdgeLabelException e) {}
        }
        throw new UnSupportedEdgeLabelException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this edge label mapping.");
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        for (final EdgeLabelMapping edgeLabelMapping : edgeLabelMappings) {
                if(edgeLabelMapping.isPossibleResult(node)){
                    return true;
                }
        }
        return false;
    }
}
