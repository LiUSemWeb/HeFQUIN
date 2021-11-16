package se.liu.ida.hefquin.engine.federation.access;

public class Neo4JException extends Exception {

    public Neo4JException(String message) {
        super(message);
    }

    public Neo4JException(String message, Throwable cause) {
        super(message, cause);
    }

    public Neo4JException(Throwable cause) {
        super(cause);
    }

}
