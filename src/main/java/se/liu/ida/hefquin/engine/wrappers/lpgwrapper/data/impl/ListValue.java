package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;

import java.util.List;

public class ListValue implements Value {

    protected final List<Object> values;

    public ListValue(final List<Object> values) {
        this.values = values;
    }

    public List<Object> getList() {
        return values;
    }
}
