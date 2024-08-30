package se.liu.ida.hefquin.engine.wrappers.lpg.data.impl;

import se.liu.ida.hefquin.engine.wrappers.lpg.data.PropertyMap;

public class LPGEdge {

    protected final String id;
    protected final String label;
    protected final PropertyMap properties;

    public LPGEdge(String id, String label, PropertyMap properties) {
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

    @Override
    public String toString() {
        return "LPGEdge{" +
                "id='" + id + '\'' +
                '}';
    }
}
