package se.liu.ida.hefquin.engine.federation.access;

public class Neo4JConnectionException extends RuntimeException {
    public Neo4JConnectionException( final String msg ) {
        super(msg);
    }
}
