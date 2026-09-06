package se.liu.ida.hefquin.federation.access.impl.reqproc;

import java.util.Date;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.engine.wrappers.graphql.conn.GraphQLConnection;
import se.liu.ida.hefquin.engine.wrappers.graphql.conn.GraphQLConnectionException;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.federation.access.JSONResponse;
import se.liu.ida.hefquin.federation.access.impl.response.JSONResponseImpl;
import se.liu.ida.hefquin.federation.members.GraphQLEndpoint;

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
	public JSONResponse performRequest( final GraphQLRequest req,
	                                    final GraphQLEndpoint fm ) {
		final Date startTime = new Date();
		final GraphQLQuery query = req.getGraphQLQuery();
		final String url = fm.getURL();

		final JsonObject jsonObj;
		try {
			jsonObj = GraphQLConnection.performRequest(query, url, connectionTimeout, readTimeout);
		}
		catch ( final GraphQLConnectionException e ) {
			final String msg = "Performing a request via the GraphQL endpoint " +
					"with service URI " + fm.getServiceURI() + " resulted in " +
					"an exception with the following message: " +
					e.getMessage();
			return new JSONResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					startTime );
		}

		return new JSONResponseImpl(jsonObj, startTime);
	}
}
