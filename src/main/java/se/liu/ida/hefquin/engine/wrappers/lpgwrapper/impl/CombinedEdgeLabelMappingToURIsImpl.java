package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedEdgeLabelException;

import java.util.List;

public class CombinedEdgeLabelMappingToURIsImpl implements EdgeLabelMapping {

    protected final List<EdgeLabelMapping> edgeLabelMappings;

    public CombinedEdgeLabelMappingToURIsImpl(final List<EdgeLabelMapping> edgeLabelMappings){
        this.edgeLabelMappings = edgeLabelMappings;
    }

    @Override
    public Node map(final String label) {
        for (EdgeLabelMapping edgeLabelMapping : edgeLabelMappings) {
            try {
                return edgeLabelMapping.map(label);
            }
            catch (IllegalArgumentException exception) {}
        }
        throw new UnSupportedEdgeLabelException("The given edge label (" + label + ") is not a supported label in the image of this edge label mapping.");
    }

    public String unmap(final Node node) {
        for (EdgeLabelMapping edgeLabelMapping : edgeLabelMappings) {
            try {
                return edgeLabelMapping.unmap(node);
            }
            catch (UnSupportedEdgeLabelException exception) {}
        }
        throw new UnSupportedEdgeLabelException("The given RDF term (" + node.toString() + ") is not an URI node or not in the image of this edge label mapping.");
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        for (EdgeLabelMapping edgeLabelMapping : edgeLabelMappings) {
                if(edgeLabelMapping.isPossibleResult(node)){
                    return true;
                }
        }
        return false;
    }
}
