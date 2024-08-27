package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;

public class LPGEdgeValue implements Value {

    protected final LPGEdge edge;

    public LPGEdgeValue(final LPGEdge edge) {
        this.edge = edge;
    }

    public LPGEdge getEdge() {
        return edge;
    }

    @Override
    public String toString() {
        return edge.toString();
    }
}
