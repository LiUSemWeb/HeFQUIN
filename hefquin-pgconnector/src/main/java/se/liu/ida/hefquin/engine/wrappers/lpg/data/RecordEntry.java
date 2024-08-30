package se.liu.ida.hefquin.engine.wrappers.lpg.data;

import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.CypherVar;

/**
 * A record entry is a pair (ai, vi) where ai is a Cypher variable and vi is a Value.
 * This interface represents a "column" of the results of evaluating a Cypher query.
 */
public interface RecordEntry {

    /**
     * Gets the name of the column
     */
    CypherVar getName();

    /**
     * Gets the value of the column in this entry
     */
    Value getValue();
}
