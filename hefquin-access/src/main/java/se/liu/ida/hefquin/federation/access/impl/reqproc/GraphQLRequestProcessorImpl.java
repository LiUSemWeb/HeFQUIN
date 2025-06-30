package se.liu.ida.hefquin.federation.access.impl.reqproc;

import java.util.Date;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.engine.wrappers.graphql.conn.GraphQLConnection;
import se.liu.ida.hefquin.engine.wrappers.graphql.conn.GraphQLConnectionException;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;
import se.liu.ida.hefquin.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.federation.access.JSONResponse;
import se.liu.ida.hefquin.federation.access.impl.response.JSONResponseImpl;

public class GraphQLRequestProcessorImpl implements GraphQLRequestProcessor {
	protected final int connectionTimeout;
	protected final int readTimeout;

	/**
	 * The given timeouts are specified in milliseconds. Any value {@literal <=} 0
	 * means no timeout.
	 */
	public GraphQLRequestProcessorImpl(final int connectionTimeout, final int readTimeout) {
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	public GraphQLRequestProcessorImpl() {
		this(-1, -1);
	}

	@Override
	public JSONResponse performRequest(final GraphQLRequest req,
			final GraphQLEndpoint fm)
			throws FederationAccessException {

		final Date startTime = new Date();
		final GraphQLQuery query = req.getGraphQLQuery();
		final String url = fm.getInterface().getURL();
		
		final JsonObject jsonObj;
		try {
			jsonObj = GraphQLConnection.performRequest(query, url, connectionTimeout, readTimeout);
		}
		catch ( final GraphQLConnectionException e ) {
			throw new FederationAccessException("Issuing a request to a GraphQL endpoint caused an exception.", e, req, fm);
		}

		return new JSONResponseImpl(jsonObj, fm, req, startTime);
	}
}
