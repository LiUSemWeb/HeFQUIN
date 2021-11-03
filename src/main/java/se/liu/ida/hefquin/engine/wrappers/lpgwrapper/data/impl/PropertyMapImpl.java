package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.PropertyMap;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;

import java.util.Map;

public class PropertyMapImpl implements PropertyMap {

    protected final Map<String, Value> properties;

    public PropertyMapImpl(final Map<String, Value> properties) {
        assert properties != null;
        this.properties = properties;
    }

    @Override
    public Value getValueFor(final String key) {
        return properties.get(key);
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public Iterable<Value> getAllValues() {
        return properties.values();
    }
}
