package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conn;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Neo4JException;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conn.Neo4jConnectionFactory.Neo4jConnection;

public class Neo4JConnectionException extends Neo4JException
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

    public Neo4jConnection getConnection() {
    	return conn;
    }

}
