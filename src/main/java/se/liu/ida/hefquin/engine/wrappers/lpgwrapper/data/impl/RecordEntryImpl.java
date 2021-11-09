package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;

public class RecordEntryImpl implements RecordEntry {

    final CypherVar name;
    final Value value;

    public RecordEntryImpl(final CypherVar name, final Value value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public CypherVar getName() {
        return name;
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{" +
                "name=" + name +
                ", value=" + value +
                '}';
    }
}
