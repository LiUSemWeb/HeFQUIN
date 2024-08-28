package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.XSD;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedNodeLabelException;

public class NodeLabelMappingImpl_SingleMatchToLiteral implements NodeLabelMapping {

    protected final String label;
    protected final Node node;

    public NodeLabelMappingImpl_SingleMatchToLiteral(final String label, final String literal){
        this.label=label;
        this.node = NodeFactory.createLiteral(literal);
    }

    @Override
    public Node map(final String label) {
        if (label.equals(this.label)) {
            return this.node;
        }
        else {
            throw new UnSupportedNodeLabelException("The given node label (" + label + ") is not a supported label in the image of this node label mapping.");
        }
    }

    public String unmap(final Node node) {
        if (!isPossibleResult(node))
            throw new UnSupportedNodeLabelException("The given RDF term (" + node.toString() + ") is not a literal.");
        return this.label;
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.equals( this.node );
    }

}
