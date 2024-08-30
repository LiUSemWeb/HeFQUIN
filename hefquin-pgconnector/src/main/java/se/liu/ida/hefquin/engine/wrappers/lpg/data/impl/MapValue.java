package se.liu.ida.hefquin.engine.wrappers.lpg.data.impl;

import java.util.Map;

import se.liu.ida.hefquin.engine.wrappers.lpg.data.Value;

public class MapValue implements Value {

    protected final Map<String, Object> values;

    public MapValue(final Map<String, Object> values) {
        this.values = values;
    }

    public Map<String, Object> getMap() {
        return values;
    }
}
