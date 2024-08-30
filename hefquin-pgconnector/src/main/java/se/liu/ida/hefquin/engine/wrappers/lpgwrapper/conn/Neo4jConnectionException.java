package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conn;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Neo4jException;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conn.Neo4jConnectionFactory.Neo4jConnection;

public class Neo4jConnectionException extends Neo4jException
{
	private static final long serialVersionUID = 6639819875154327577L;

	protected final Neo4jConnection conn;

	public Neo4jConnectionException( final Neo4jConnection conn,
	                                 final String message,
	                                 final Throwable cause ) {
		super(message, cause);

		assert conn != null;
		this.conn = conn;
	}

	public Neo4jConnectionException( final Neo4jConnection conn,
	                                 final String message  ) {
		super(message);

		assert conn != null;
		this.conn = conn;
	}

	public Neo4jConnectionException( final Neo4jConnection conn,
	                                 final Throwable cause ) {
		super(cause);

		assert conn != null;
		this.conn = conn;
	}

	public Neo4jConnection getConnection() {
		return conn;
	}

}
