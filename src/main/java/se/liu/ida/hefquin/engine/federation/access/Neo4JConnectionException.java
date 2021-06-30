package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.federation.access.Neo4jConnectionFactory.Neo4jConnection;

public class Neo4JConnectionException extends Exception
{
	private static final long serialVersionUID = 6639819875154327577L;

	protected final Neo4jConnection conn;

    public Neo4JConnectionException( final String message,
                                     final Throwable cause,
                                     final Neo4jConnection conn ) {
        super(message, cause);

        assert conn != null;
        this.conn = conn;
    }

    public Neo4JConnectionException( final String message,
                                     final Neo4jConnection conn ) {
        super(message);

        assert conn != null;
        this.conn = conn;
    }

    public Neo4JConnectionException( final Throwable cause,
                                     final Neo4jConnection conn ) {
        super(cause);

        assert conn != null;
        this.conn = conn;
    }

    public Neo4JConnectionException( final Neo4jConnection conn ) {
        super();

        assert conn != null;
        this.conn = conn;
    }

    public Neo4jConnection getConnection() {
    	return conn;
    }

}
