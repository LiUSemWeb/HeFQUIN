package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;

import java.util.Map;

public class MapValue implements Value {

    protected final Map<String, Object> values;

    public MapValue(final Map<String, Object> values) {
        this.values = values;
    }

    public Map<String, Object> getMap() {
        return values;
    }
}
