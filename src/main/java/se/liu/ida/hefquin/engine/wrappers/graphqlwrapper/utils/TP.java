package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import se.liu.ida.hefquin.engine.query.TriplePattern;

/**
 * Wrapper class for a TriplePattern that includes an integer id
 */
public class TP {
    private final int id;
    private final TriplePattern triplePattern;

    public TP(final int id, final TriplePattern triplePattern) {
        this.id = id;
        this.triplePattern = triplePattern;
    }

    public final int getId() {
        return id;
    }

    public final TriplePattern getTriplePattern() {
        return triplePattern;
    }
}
