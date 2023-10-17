package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.XSD;

public class NodeLabelMappingToLiteralsImpl implements NodeLabelMapping{

    @Override
    public Node map(final String label) {
        return NodeFactory.createLiteral(label);
    }

    @Override
    public String unmap(final Node node) {
        if (!node.isLiteral() || !node.getLiteral().getDatatypeURI().equals(XSD.xstring.getURI()))
            throw new IllegalArgumentException("The given RDF term (" + node.toString() + ") is not a literal.");
        return node.getLiteralLexicalForm();
    }

    @Override
    public boolean isPossibleResult(final Node node) {
        return node.isLiteral() && node.getLiteral().getDatatypeURI().equals(XSD.xstring.getURI());
    }
}
