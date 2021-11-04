package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

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
    public String mapNode(final LPGNode node) {
        return NS + NODE + node.getId();
    }

    @Override
    public LPGNode unmapNode(String iri) {
        final String id = iri.replaceAll(NS + NODE, "");
        return new LPGNode(id, "", null);
    }

    @Override
    public String mapNodeLabel(final String label) {
        return NS + NODELABEL + label;
    }

    @Override
    public String unmapNodeLabel(String label) {
        return label.replaceAll(NS + NODELABEL, "");
    }

    @Override
    public String mapEdgeLabel(final String label) {
        return NS + RELATIONSHIP + label;
    }

    @Override
    public String unmapEdgeLabel(String label) {
        return label.replaceAll(NS + RELATIONSHIP, "");
    }

    @Override
    public String mapProperty(final String property) {
        return NS + PROPERTY + property;
    }

    @Override
    public String unmapProperty(String iri) {
        return iri.replaceAll(NS + PROPERTY, "");
    }

    @Override
    public String getLabel() {
        return LABEL;
    }
}
