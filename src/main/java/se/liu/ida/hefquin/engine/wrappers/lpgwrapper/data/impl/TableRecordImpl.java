package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;

import java.util.Collection;
import java.util.List;

public class TableRecordImpl implements TableRecord {

    final List<RecordEntry> entries;

    public TableRecordImpl(final List<RecordEntry> entries) {
        this.entries = entries;
    }

    @Override
    public Collection<RecordEntry> getRecords() {
        return entries;
    }
}
