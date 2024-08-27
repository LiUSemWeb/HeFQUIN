package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

public class QueryTranslatingException extends Exception {

    public QueryTranslatingException(final String message, final Exception e) {
        super(message,e);
    }

    public QueryTranslatingException(final String message) {
        super(message);
    }
}
