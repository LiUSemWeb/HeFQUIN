package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.PropertyMap;

public class LPGNode {

    protected final String id;
    protected final String label;
    protected final PropertyMap properties;

    public LPGNode(final String id, final String label, final PropertyMap properties) {
        this.id = id;
        this.label = label;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public PropertyMap getProperties() {
        return properties;
    }
}
