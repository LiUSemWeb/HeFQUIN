package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.XSD;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.exceptions.UnSupportedNodeLabelException;

public class NodeLabelMappingImpl_AllToLiterals implements NodeLabelMapping{

    @Override
    public Node map(final String label) {
        return NodeFactory.createLiteral(label);
    }

    @Override
    public String unmap(final Node node) {
        if (!isPossibleResult(node))
            throw new UnSupportedNodeLabelException("The given RDF term (" + node.toString() + ") is not a literal.");
        return node.getLiteralLexicalForm();
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.isLiteral() && XSD.xstring.getURI().equals(node.getLiteral().getDatatypeURI());
    }
}
