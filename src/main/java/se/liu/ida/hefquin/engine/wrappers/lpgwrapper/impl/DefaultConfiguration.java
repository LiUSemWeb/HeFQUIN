package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;

public class DefaultConfiguration implements LPG2RDFConfiguration {

    protected final String NS = "https://example.org/";
    protected final String NODE = "node/";
    protected final String NODELABEL = "label/";
    protected final String RELATIONSHIP = "relationship/";
    protected final String PROPERTY = "property/";
    protected final String LABEL = "http://www.w3.org/2000/01/rdf-schema#Label";

    @Override
    public String mapNode(final String nodeID) {
        return NS + NODE + nodeID;
    }

    @Override
    public String mapNodeLabel(final String label) {
        return NS + NODELABEL + label;
    }

    @Override
    public String mapEdgeLabel(final String label) {
        return NS + RELATIONSHIP + label;
    }

    @Override
    public String mapProperty(final String property) {
        return NS + PROPERTY + property;
    }

    @Override
    public String getLabel() {
        return LABEL;
    }
}
