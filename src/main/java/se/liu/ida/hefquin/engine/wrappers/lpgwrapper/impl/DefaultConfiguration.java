package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

public class DefaultConfiguration implements LPG2RDFConfiguration {

    protected final String NS = "https://example.org/";
    protected final String NODE = "node/";
    protected final String NODELABEL = "label/";
    protected final String RELATIONSHIP = "relationship/";
    protected final String PROPERTY = "property/";
    protected final String LABEL = "http://www.w3.org/2000/01/rdf-schema#Label";

    @Override
    public Node mapNode(final LPGNode node) {
        return NodeFactory.createURI(NS + NODE + node.getId());
    }

    @Override
    public LPGNode unmapNode(String iri) {
        final String id = iri.replaceAll(NS + NODE, "");
        return new LPGNode(id, "", null);
    }

    @Override
    public Node mapNodeLabel(final String label) {
        return NodeFactory.createURI(NS + NODELABEL + label);
    }

    @Override
    public String unmapNodeLabel(String label) {
        return label.replaceAll(NS + NODELABEL, "");
    }

    @Override
    public Node mapEdgeLabel(final String label) {
        return NodeFactory.createURI(NS + RELATIONSHIP + label);
    }

    @Override
    public String unmapEdgeLabel(String label) {
        return label.replaceAll(NS + RELATIONSHIP, "");
    }

    @Override
    public Node mapProperty(final String property) {
        return NodeFactory.createURI(NS + PROPERTY + property);
    }

    @Override
    public String unmapProperty(String iri) {
        return iri.replaceAll(NS + PROPERTY, "");
    }

    @Override
    public Node getLabel() {
        return NodeFactory.createURI(LABEL);
    }
}
