package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import java.io.InputStream;

import org.apache.jena.sparql.engine.http.HttpQuery;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessorBase.HttpRequestException;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;

public class GraphQLRequestProcessorImpl implements GraphQLRequestProcessor
{
	protected final int connectionTimeout;
	protected final int readTimeout;

	/**
	 * The given timeouts are specified in milliseconds. Any value {@literal <=} 0 means no timeout.
	 */
	public GraphQLRequestProcessorImpl( final int connectionTimeout, final int readTimeout ) {
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	public GraphQLRequestProcessorImpl() {
		this(-1, -1);
	}

	@Override
	public JSONResponse performRequest( final GraphQLRequest req,
	                                    final GraphQLEndpoint fm )
	                                    		throws FederationAccessException {
		try {
			return performRequest( fm.getInterface().createHttpRequest(req),
			                       req.getGraphQLQuery() );
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Performing a GraphQL request caused an exception.", ex, req, fm);
		}
	}

	protected JSONResponse performRequest( final HttpQuery req,
	                                       final GraphQLQuery query )
	                                                 throws HttpRequestException {
		req.setConnectTimeout(connectionTimeout);
		req.setReadTimeout(readTimeout);

		// execute the request
		final InputStream inStream;
		try {
			inStream = req.exec();
		}
		catch ( final Exception ex ) {
			throw new HttpRequestException("Executing an HTTP request for a GraphQL server caused an exception.", ex);
		}

		return parseRetrievedData(inStream, query);
	}

	protected JSONResponse parseRetrievedData( final InputStream inStream,
	                                           final GraphQLQuery query )
	                                                  throws HttpRequestException {
		// TODO this function needs to be implemented

		return null;
	}

}
