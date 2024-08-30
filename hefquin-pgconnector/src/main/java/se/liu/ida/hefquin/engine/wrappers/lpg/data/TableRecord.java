package se.liu.ida.hefquin.engine.wrappers.lpg.data;

/**
 * A record is a "row" of the results of the evaluation of a Cypher query.
 * Formally is a named tuple r=(a1: v1, ..., an: vn) where ai are names (variables)
 * and vi are "values". A value can be a literal, a node, an edge, a list or a map.
 *
 * Each element of the record (ai: vi), is called an entry.
 */
public interface TableRecord {

    /**
     * Gets the collection of entries of the row/record.
     */
    Iterable<RecordEntry> getRecordEntries();

    /**
     * Returns the number of columns of the Record
     */
    int size();

    /**
     * Returns the {@link RecordEntry} in the i-th position
     */
    RecordEntry getEntry(int i);
}
