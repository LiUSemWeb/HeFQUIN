package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;

public class LiteralValue implements Value {

    protected final Object internalValue;

    public LiteralValue(final Object internalValue) {
        this.internalValue = internalValue;
    }

    public Object getValue() {
        return internalValue;
    }

    @Override
    public String toString() {
        return internalValue.toString();
    }
}
